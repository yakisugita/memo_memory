package net.yakijake.memory

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import net.yakijake.memory.ui.theme.MemoryTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MemoryTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("Android")
                    Test()
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}
@Composable
fun Test(modifier: Modifier = Modifier) {
    var sliderPosition by remember { mutableStateOf(0.5f) }
    var sliderPosition2 by remember { mutableStateOf(0.5f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var scale by remember { mutableStateOf(1f) }
    var CustomShape by remember { mutableStateOf(GenericShape { size, layoutDirection -> }) }
    Text(
        text = sliderPosition.toString(),
        modifier = Modifier.padding(all = 8.dp)
    )
    Image(
        painter = painterResource(id = R.drawable.original),
        contentDescription = "An Image",
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
//                    scale *= zoom
                    offset += pan
                    CustomShape = GenericShape { size, layoutDirection ->
                        moveTo(0f, 0f)
                        lineTo((size.width*sliderPosition)+offset.x, 0f)
                        lineTo((size.width*sliderPosition)+offset.x, size.height)
                        lineTo(0f, size.height)
                    }
                }
            }
            .graphicsLayer {
                scaleX = 2f
                scaleY = 2f
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
                scaleX = 2f
                scaleY = 2f
                translationX = offset.x
//                translationY = offset.y
            }
    )
    Column {
        Slider(
            value = sliderPosition,
            onValueChange = {
                sliderPosition = it
                CustomShape = GenericShape { size, layoutDirection ->
                    moveTo(0f, 0f)
                    lineTo((size.width*sliderPosition)+offset.x, 0f)
                    lineTo((size.width*sliderPosition)+offset.x, size.height)
                    lineTo(0f, size.height)
                }
            }
        )
        Slider(
            value = sliderPosition2,
            onValueChange = {
                sliderPosition2 = it
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MemoryTheme {
        Greeting("Android")
        Test()
    }
}