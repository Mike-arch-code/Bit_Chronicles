package com.bit_chronicles.viewmodel.map

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bit_chronicles.R

class ImageFragment : Fragment() {

    companion object {
        private const val ARG_IMAGE_DATA = "arg_image_data"

        fun newInstance(imageData: String): ImageFragment {
            val fragment = ImageFragment()
            val args = Bundle()
            args.putString(ARG_IMAGE_DATA, imageData)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_image, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imageView = view.findViewById<ImageView>(R.id.imageView)
        val imageData = arguments?.getString(ARG_IMAGE_DATA) ?: return

        val pixelArray = parseImageData(imageData)

        val width = pixelArray[0].size
        val height = pixelArray.size

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val color = when (pixelArray[y][x]) {
                    0 -> Color.rgb(173, 255, 47) // Verde claro (Lime Green)
                    1 -> Color.rgb(34, 139, 34)  // Verde oscuro (Forest Green)
                    2 -> Color.rgb(25, 25, 112)  // Azul oscuro (Midnight Blue)
                    else -> Color.BLACK          // Por si acaso hay un valor inesperado
                }
                bitmap.setPixel(x, y, color)
            }
        }

        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 300, 300, false)
        imageView.setImageBitmap(scaledBitmap)
    }


    private fun parseImageData(data: String): Array<IntArray> {
        val lines = data.trim()
            .split("\n")
            .map { it.trim() }
            .filter { it.length == 32 }

        val height = lines.size
        val width = if (lines.isNotEmpty()) lines[0].length else 0

        val result = Array(height) { IntArray(width) }
        for (y in lines.indices) {
            for (x in 0 until width) {
                result[y][x] = lines[y][x].digitToIntOrNull() ?: 0
            }
        }

        return result
    }

}

