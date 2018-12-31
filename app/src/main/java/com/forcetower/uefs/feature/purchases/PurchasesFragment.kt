/*
 * Copyright (c) 2018.
 * João Paulo Sena <joaopaulo761@gmail.com>
 *
 * This file is part of the UNES Open Source Project.
 *
 * UNES is licensed under the MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.forcetower.uefs.feature.purchases

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.SkuDetails
import com.android.billingclient.api.SkuDetailsParams
import com.forcetower.uefs.R
import com.forcetower.uefs.core.billing.SkuDetailsResult
import com.forcetower.uefs.core.injection.Injectable
import com.forcetower.uefs.core.vm.BillingViewModel
import com.forcetower.uefs.core.vm.EventObserver
import com.forcetower.uefs.core.vm.UViewModelFactory
import com.forcetower.uefs.feature.shared.UFragment
import com.forcetower.uefs.feature.shared.extensions.provideViewModel
import com.forcetower.uefs.databinding.FragmentPurchasesBinding
import java.util.Calendar
import javax.inject.Inject

class PurchasesFragment : UFragment(), Injectable, PurchasesUpdatedListener, BillingClientStateListener {
    @Inject
    lateinit var factory: UViewModelFactory
    @Inject
    lateinit var preferences: SharedPreferences

    private lateinit var viewModel: BillingViewModel
    private lateinit var binding: FragmentPurchasesBinding
    private lateinit var skuAdapter: SkuDetailsAdapter
    private lateinit var billingClient: BillingClient

    private val list: MutableList<String> = mutableListOf()

    companion object {
        const val TAG = "PurchaseFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        billingClient = BillingClient.newBuilder(requireContext().applicationContext)
                .setListener(this)
                .build()

        if (!billingClient.isReady) {
            Log.d(TAG,"Starting Connection...")
            billingClient.startConnection(this)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel = provideViewModel(factory)
        return FragmentPurchasesBinding.inflate(inflater, container, false).also {
            binding = it
        }.apply {
            imageTop = "https://cdn.dribbble.com/users/1903950/screenshots/4225909/02_main_tr__1.gif"
            executePendingBindings()
            incToolbar.textToolbarTitle.text = getString(R.string.label_purchases)
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        skuAdapter = SkuDetailsAdapter(viewModel)
        binding.recyclerSku.apply {
            adapter = skuAdapter
        }
        viewModel.getSkus().observe(this, Observer {
            if (list != it) {
                list.clear()
                list.addAll(it)
                if (it.isNotEmpty()) {
                    getSkuDetails(it)
                }
            }
        })
        viewModel.selectSku.observe(this, EventObserver {
            purchaseFlow(it)
        })
    }

    private fun getSkuDetails(list: List<String>) {
        val params = SkuDetailsParams.newBuilder()
                .setSkusList(list)
                .setType(BillingClient.SkuType.INAPP)
                .build()
        billingClient.querySkuDetailsAsync(params) { code, details ->
            processDetails(SkuDetailsResult(code, details))
        }
    }

    private fun processDetails(result: SkuDetailsResult) {
        if (result.responseCode == BillingClient.BillingResponse.OK) {
            val values = result.list
            Log.d(TAG,"Values: $values")
            skuAdapter.submitList(values)
        } else {
            showSnack(getString(R.string.donation_service_response_error), true)
        }
    }

    private fun purchaseFlow(details: SkuDetails) {
        val params = BillingFlowParams.newBuilder()
                .setSkuDetails(details)
                .build()

        billingClient.launchBillingFlow(requireActivity(), params)
    }

    private fun updatePurchases() {
        if (!billingClient.isReady) {
            Log.d(TAG,"BillingClient is not ready to query for existing purchases")
        }
        val result = billingClient.queryPurchases(BillingClient.SkuType.SUBS)
        if (result == null) {
            Log.d(TAG,"Update purchase: Null purchase result")
        } else {
            if (result.purchasesList == null) {
                Log.d(TAG,"Update purchase: Null purchase list")
            } else {
                handlePurchases(result.purchasesList)
            }
        }
    }

    private fun handlePurchases(purchasesList: List<Purchase>?) {
        purchasesList ?: return
        purchasesList.forEach { purchase ->
            // TODO extract this method to somewhere else it will be usefull
            if (purchase.sku == "score_increase_common") {
                Log.d(TAG,"Purchased score increase common")
                val current = preferences.getInt("score_increase_value", 0)
                val expires = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }.timeInMillis
                preferences.edit()
                        .putInt("score_increase_value", current + 1)
                        .putLong("score_increase_expires", expires)
                        .apply()
            }
            billingClient.consumeAsync(purchase.purchaseToken) { code, token ->
                Log.d(TAG,"Attempt to consume $token finished with code $code ")
            }
        }
    }

    @SuppressLint("BinaryOperationInTimber")
    override fun onPurchasesUpdated(responseCode: Int, purchasesList: List<Purchase>?) {
        Log.d(TAG,"onPurchasesUpdated, response code: $responseCode")
        when (responseCode) {
            BillingClient.BillingResponse.OK -> {
                if (purchasesList == null) {
                    Log.d(TAG,"Purchase update: No purchases")
                    handlePurchases(null)
                } else {
                    handlePurchases(purchasesList)
                }
            }
            BillingClient.BillingResponse.USER_CANCELED -> {
                Log.d(TAG,"User canceled the purchase")
            }
            BillingClient.BillingResponse.ITEM_ALREADY_OWNED -> {
                Log.d(TAG,"The user already owns this item")
            }
            BillingClient.BillingResponse.DEVELOPER_ERROR -> {
                Log.d(TAG,"Developer error means that Google Play does not recognize the " +
                        "configuration. If you are just getting started, make sure you have " +
                        "configured the application correctly in the Google Play Console. " +
                        "The SKU product ID must match and the APK you are using must be " +
                        "signed with release keys.")
            }
            else -> {
                Log.d(TAG,"See error code in BillingClient.BillingResponse: $responseCode")
            }
        }
    }

    override fun onBillingSetupFinished(billingResponseCode: Int) {
        Log.d(TAG,"onBillingSetupFinished: $billingResponseCode")
        if (billingResponseCode == BillingClient.BillingResponse.OK) {
            updatePurchases()
        }
    }

    override fun onBillingServiceDisconnected() {
        Log.d(TAG,"onBillingServiceDisconnected")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (billingClient.isReady) {
            Log.d(TAG,"Closing connection")
            billingClient.endConnection()
        }
    }
}