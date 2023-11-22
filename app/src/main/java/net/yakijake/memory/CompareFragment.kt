package net.yakijake.memory

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CompareFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CompareFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_compare, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CompareFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CompareFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}

@Composable
fun Test(modifier: Modifier = Modifier) {
    var sliderPosition by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var scale by remember { mutableStateOf(2f) }
    var zoom_ by remember { mutableStateOf(0f) }
    var centeroid_ by remember { mutableStateOf(Offset.Zero) }
    var pan_ by remember { mutableStateOf(Offset.Zero) }
    var CustomShape by remember { mutableStateOf(GenericShape { size, layoutDirection -> }) }
    Image(
        painter = painterResource(id = R.drawable.original),
        contentDescription = "An Image",
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .pointerInput(Unit) {
                detectTransformGestures(true) { centeroid, pan, zoom, _ ->

                    scale *= zoom
                    if (scale < 1) scale = 1f
                    if (scale > 10) scale = 10f
                    offset += pan

                    pan_ = pan
                    zoom_ = zoom
                    centeroid_ = centeroid
//                    CustomShape = GenericShape { size, layoutDirection ->
//                        moveTo(0f, 0f)
//                        lineTo(size.width*sliderPosition, 0f)
//                        lineTo(size.width*sliderPosition, size.height)
//                        lineTo(0f, size.height)
//                    }
                }
            }
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationX = offset.x
//                translationY = offset.y
            }
    )
    Image(
        painter = painterResource(id = R.drawable.masked),
        contentDescription = "An Image",
        contentScale = ContentScale.Fit,
        modifier = Modifier
//            .size(200.dp)
//            .height(1000.dp)
//            .offset((sliderPosition2*100).dp, 0.dp)
            .fillMaxHeight()
            .clip(CustomShape) // 作成したカスタムShapeで切り抜く
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationX = offset.x
//                translationY = offset.y
            }
    )

    Column {
        Text(
            text = sliderPosition.toString(),
            modifier = Modifier.padding(all = 8.dp)
        )
        Text(
            text = "${offset.x} / $scale = ${(offset.x/scale)}",
            modifier = Modifier.padding(all = 8.dp)
        )
        Text(
            text = "${centeroid_.x} , ${centeroid_.y}",
            modifier = Modifier.padding(all = 8.dp)
        )
        Slider(
            value = sliderPosition,
            onValueChange = {
                sliderPosition = it
                CustomShape = GenericShape { size, layoutDirection ->
                    moveTo(0f, 0f)
                    lineTo(size.width*sliderPosition, 0f)
                    lineTo(size.width*sliderPosition, size.height)
                    lineTo(0f, size.height)
                }
            }
        )
    }
}

@Composable
fun CompareContent(
    navController: NavController
) {
    // content
    Text(
        text = "Compare",
        modifier = Modifier.padding(all = 8.dp)
    )
    Test()
}