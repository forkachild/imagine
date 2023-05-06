package com.suhel.imagine.editor.ui.main

import android.os.Bundle
import android.view.View
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.snackbar.Snackbar
import com.suhel.imagine.core.ImagineEngine
import com.suhel.imagine.editor.databinding.ActivityMainBinding
import com.suhel.imagine.editor.helper.BitmapSaveTask
import com.suhel.imagine.editor.helper.DragSwipeCallback
import com.suhel.imagine.editor.model.UriImageProvider
import com.suhel.imagine.editor.model.layers.EffectLayer
import com.suhel.imagine.editor.model.BitmapSaveFormat
import com.suhel.imagine.editor.ui.addlayer.AddLayerDialog
import com.suhel.imagine.editor.ui.saveformat.BitmapSaveFormatDialog
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var imagineEngine: ImagineEngine
    private lateinit var adapter: LayerAdapter

    private var saveFormat: BitmapSaveFormat = BitmapSaveFormat.PNG
    private val layers: MutableList<EffectLayer> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupImagine()
        setupLayerList()
        setupUI()
    }

    private fun setupImagine() {
        imagineEngine = ImagineEngine(binding.imagine)
        binding.imagine.engine = imagineEngine

        imagineEngine.layers = layers
        imagineEngine.onBitmap = { bitmap ->
            Thread(
                BitmapSaveTask(
                    this,
                    bitmap,
                    saveFormat,
                    { showSnackbar("Save successful") },
                    { showSnackbar(it.message.toString()) }
                )
            ).start()
        }
    }

    private fun setupLayerList() {
        val itemTouchHelper = ItemTouchHelper(
            DragSwipeCallback(
                this,
                onItemMove = { from, to ->
                    if (from < to) {
                        (from until to)
                            .forEach { Collections.swap(layers, it, it + 1) }
                    } else {
                        (from downTo (to + 1))
                            .forEach { Collections.swap(layers, it, it - 1) }
                    }

                    adapter.notifyItemMoved(from, to)
                    imagineEngine.updatePreview()
                    true
                },
                onItemSwipe = { idx ->
                    layers.removeAt(idx)
                    updatePlaceholderVisibility()
                    adapter.notifyItemRemoved(idx)
                    imagineEngine.updatePreview()
                }
            )
        )

        adapter = LayerAdapter()
        adapter.data = layers
        adapter.onLayerUpdated = { index, intensity ->
            layers[index].factor = intensity
            imagineEngine.updatePreview()
        }
        binding.lstLayers.layoutManager = LinearLayoutManager(this)
        binding.lstLayers.adapter = adapter
        itemTouchHelper.attachToRecyclerView(binding.lstLayers)
        adapter.onStartDrag = {
            itemTouchHelper.startDrag(it)
        }
    }

    private fun setupUI() {
        val imagePicker = registerForActivityResult(
            ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            if (uri != null) {
                imagineEngine.imageProvider = UriImageProvider(this, uri)
                imagineEngine.updatePreview()
            }
        }

        binding.btnPick.setOnClickListener {
            imagePicker.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }

        binding.btnExport.setOnClickListener {
            val dialog = BitmapSaveFormatDialog()
            dialog.onChoose = {
                saveFormat = it
                showSnackbar("Saving")
                imagineEngine.exportBitmap()
            }
            dialog.show(supportFragmentManager, "BitmapSaveFormatDialog")
        }

        binding.btnAddLayer.setOnClickListener {
            val dialog = AddLayerDialog()
            dialog.onAddLayer = {
                layers.add(it)
                updatePlaceholderVisibility()
                adapter.data = layers
                imagineEngine.updatePreview()
            }
            dialog.show(supportFragmentManager, "AddLayerDialog")
        }
    }

    override fun onResume() {
        super.onResume()
        binding.imagine.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.imagine.onPause()
    }

    private fun updatePlaceholderVisibility() {
        binding.lblLayersPlaceholder.visibility = if (layers.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun showSnackbar(message: String) {
        Snackbar
            .make(binding.root, message, Snackbar.LENGTH_SHORT)
            .show()
    }

}