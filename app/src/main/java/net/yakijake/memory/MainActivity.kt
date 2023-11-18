package net.yakijake.memory

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
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
    var sliderPosition by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var scale by remember { mutableStateOf(2f) }
    var CustomShape by remember { mutableStateOf(GenericShape { size, layoutDirection -> }) }
    Image(
        painter = painterResource(id = R.drawable.original),
        contentDescription = "An Image",
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale *= zoom
                    if (scale < 1) scale = 1f
                    if (scale > 10) scale = 10f
                    offset += pan
                    CustomShape = GenericShape { size, layoutDirection ->
                        moveTo(0f, 0f)
                        lineTo((size.width*sliderPosition)+offset.x+(sliderPosition-0.5f)*scale*920, 0f)
                        lineTo((size.width*sliderPosition)+offset.x+(sliderPosition-0.5f)*scale*920, size.height)
                        lineTo(0f, size.height)
                    }
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
            text = (offset.x).toString(),
            modifier = Modifier.padding(all = 8.dp)
        )
        Text(
            text = (scale).toString(),
            modifier = Modifier.padding(all = 8.dp)
        )
        Text(
            text = (offset.x/scale).toString(),
            modifier = Modifier.padding(all = 8.dp)
        )
        Text(
            text = ((sliderPosition-0.5f)*scale).toString(),
            modifier = Modifier.padding(all = 8.dp)
        )
        Slider(
            value = sliderPosition,
            onValueChange = {
                sliderPosition = it
                CustomShape = GenericShape { size, layoutDirection ->
                    moveTo(0f, 0f)
//                    lineTo((size.width*sliderPosition)+(offset.x/scale), 0f)
                    lineTo((size.width*sliderPosition)+offset.x+(sliderPosition-0.5f)*scale*920, 0f)
                    lineTo((size.width*sliderPosition)+offset.x+(sliderPosition-0.5f)*scale*920, size.height)
                    lineTo(0f, size.height)
                }
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