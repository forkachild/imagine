package com.suhel.imagine.editor.ui.main

import android.os.Bundle
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.suhel.imagine.editor.databinding.ActivityMainBinding
import com.suhel.imagine.editor.helper.BitmapSaveTask
import com.suhel.imagine.editor.layers.EffectLayer
import com.suhel.imagine.editor.ui.addlayer.AddLayerDialog
import com.suhel.imagine.editor.ui.saveformat.BitmapSaveFormatDialog
import com.suhel.imagine.types.UriImageProvider
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: LayerAdapter

    private val layers: MutableList<EffectLayer> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imagePicker = registerForActivityResult(
            ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            if (uri != null) {
                binding.imagine.imageProvider = UriImageProvider(this, uri)
                binding.imagine.preview()
            }
        }

        binding.imagine.layers = layers
        binding.imagine.onBitmap = { bitmap ->
            val dialog = BitmapSaveFormatDialog()
            dialog.onChoose = { format ->
                Thread(
                    BitmapSaveTask(
                        this,
                        bitmap,
                        format,
                        { showSnackbar("Save successful") },
                        { showSnackbar(it.message.toString()) }
                    )
                ).start()
                showSnackbar("Saving")
            }
            dialog.show(supportFragmentManager, "BitmapSaveFormatDialog")
        }

        adapter = LayerAdapter()
        adapter.data = layers
        adapter.onLayerUpdated = { index, intensity ->
            layers[index].factor = intensity
            binding.imagine.preview()
        }

        binding.lstLayers.layoutManager = LinearLayoutManager(this)
        binding.lstLayers.adapter = adapter
        ItemTouchHelper(
            LayerRearrangeHelper(
                onItemMove = { from, to ->
                    if (from < to) {
                        (from until to)
                            .forEach { Collections.swap(layers, it, it + 1) }
                    } else {
                        (from downTo (to + 1))
                            .forEach { Collections.swap(layers, it, it - 1) }
                    }

                    adapter.notifyItemMoved(from, to)
                    binding.imagine.preview()
                    true
                },
                onItemSwipe = { idx ->
                    layers.removeAt(idx)
                    adapter.notifyItemRemoved(idx)
                    binding.imagine.preview()
                }
            )
        ).attachToRecyclerView(binding.lstLayers)

        binding.btnPick.setOnClickListener {
            imagePicker.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }

        binding.btnExport.setOnClickListener {
            binding.imagine.export()
        }

        binding.btnAddLayer.setOnClickListener {
            val dialog = AddLayerDialog()
            dialog.onAddLayer = {
                layers.add(it)
                adapter.data = layers
                binding.imagine.preview()
            }
            dialog.show(supportFragmentManager, "AddLayerDialog")
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar
            .make(binding.root, message, Snackbar.LENGTH_SHORT)
            .show()
    }

}