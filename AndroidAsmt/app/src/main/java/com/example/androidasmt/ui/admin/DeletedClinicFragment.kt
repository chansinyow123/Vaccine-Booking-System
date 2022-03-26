package com.example.androidasmt.ui.admin

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
import com.example.androidasmt.databinding.FragmentDeletedClinicBinding
import com.example.androidasmt.databinding.FragmentEditClinicBinding
import com.example.androidasmt.network.VaccineApi
import com.example.androidasmt.rv.ClinicAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DeletedClinicFragment : Fragment() {

    private lateinit var binding: FragmentDeletedClinicBinding
    private val nav by lazy { findNavController() }

    private val vm: DeletedClinicFragmentModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentDeletedClinicBinding.inflate(inflater, container, false)

        binding.vm = vm
        binding.lifecycleOwner = this

        val adapter = ClinicAdapter { holder, item ->
            // If click on entire item, navigate user to edit clinic
            holder.root.setOnClickListener {
                nav.navigate(
                    R.id.action_deletedClinicFragment_to_undoDeletedClinicFragment,
                    bundleOf("id" to item.id)
                )
            }
        }
        binding.rv.adapter = adapter
        binding.rv.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        vm.list.observe(viewLifecycleOwner) { l -> adapter.submitList(l) }

        // get data from api
        initData()

        return binding.root
    }

    private fun initData() {
        lifecycleScope.launch {
            val response = withContext(Dispatchers.IO) {
                VaccineApi.retrofitService.getDeletedClinicList()
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