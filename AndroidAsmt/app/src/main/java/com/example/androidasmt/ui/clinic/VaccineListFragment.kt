package com.example.androidasmt.ui.clinic

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.androidasmt.R
import com.example.androidasmt.databinding.FragmentVaccineListBinding
import com.example.androidasmt.helper.hideKeyboard
import com.example.androidasmt.network.VaccineApi
import com.example.androidasmt.rv.ClinicAdapter
import com.example.androidasmt.rv.VaccineAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VaccineListFragment : Fragment() {

    private lateinit var binding: FragmentVaccineListBinding
    private val nav by lazy { findNavController() }

    private val vm: VaccineListFragmentModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentVaccineListBinding.inflate(inflater, container, false)

        binding.vm = vm
        binding.lifecycleOwner = this

        val adapter = VaccineAdapter { holder, item ->
            // If click on entire item, navigate user to edit vaccine
            holder.root.setOnClickListener {
                nav.navigate(
                    R.id.action_vaccineListFragment_to_editVaccineFragment,
                    bundleOf("id" to item.id)
                )
            }
        }
        binding.rv.adapter = adapter
        binding.rv.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        vm.list.observe(viewLifecycleOwner) { l -> adapter.submitList(l) }

        initData()

        binding.floatingActionButton.setOnClickListener {
            hideKeyboard()
            nav.navigate(R.id.action_vaccineListFragment_to_addVaccineFragment)
        }

        return binding.root
    }

    private fun initData() {
        lifecycleScope.launch {
            val response = withContext(Dispatchers.IO) {
                VaccineApi.retrofitService.getAllVaccineByClinic()
            }

            when(response.code()) {
                in 200..299 -> vm.initData(response.body()!!)
                else -> statusOther(response.code().toString())
            }
        }
    }

    private fun statusOther(statusCode: String) {
        // display error alert dialog
        AlertDialog.Builder(requireContext())
            .setIcon(R.drawable.ic_error)
            .setTitle("Error")
            .setMessage("Unexpected Error Occurred: $statusCode")
            .setPositiveButton("Try again later", null)
            .show()
    }
}