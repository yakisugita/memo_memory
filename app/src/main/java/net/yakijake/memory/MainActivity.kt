package net.yakijake.memory

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import net.yakijake.memory.ui.theme.MemoryTheme
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("Memory_Log",filesDir.toString())
        val path = filesDir.toString()

        Log.d("Memory_Log_DirCheck","checking")

        if (!File(path, "db").exists()) {
            Files.createDirectory(Paths.get("$path/db"))
            Log.d("Memory_Log_DirCheck","created db")
        }
        if (!File(path, "memo").exists()) {
            Files.createDirectory(Paths.get("$path/memo"))
            Log.d("Memory_Log_DirCheck","created memo")
        }

        setContent {
            MemoryTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
//                    Greeting("Android")
//                    Test()
                    SetNav()
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
fun SetNav() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "topContent") {
//        composable("profile") { Test(/*...*/) }
        composable("topContent") {
            TopContent(navController = navController)
        }
        composable(
            "compareContent/{dirName}",
            arguments = listOf(
                navArgument("dirName") {
                    // 渡したい値の設定
                    type = NavType.StringType
                    nullable = false
                    defaultValue = ""
                }
            )
        ) {
            // NavBackStackEntryから値を取得して、次の画面に渡す。
            val dirName = it.arguments?.getString("dirName")
            CompareContent(dirName!!,navController = navController)
        }

//        composable("friendslist") { FriendsList(/*...*/) }
        /*...*/
    }
}

@Composable
fun TopContent(
    navController: NavController
) {
    Column {
//        var count = ""

        Text(
            text = "TopPage",
            modifier = Modifier.padding(all = 8.dp)
        )
        Button(
            onClick = {navController.navigate("compareContent")}
        ) {
            Text( text = "compareに遷移" )
        }

        val context = LocalContext.current
        LazyColumn {
            val path = context.filesDir.toString()

            val dirlist = File("$path/memo").list()
            dirlist.forEach {
                if (File("$path/memo", it).isDirectory) {
                    Log.d("Memory_Log_FileList", it)
                }
            }

            items(dirlist) {
                Text(
                    text = it,
                    modifier = Modifier
                        .padding(all = 16.dp)
                        .clickable {
                            Log.d("Memory_Log_Click", it)
                            navController.navigate("compareContent/$it")
                        }
                )
            }
        }
    }
}


@Composable
fun Test(dirName : String, modifier: Modifier = Modifier) {
    var sliderPosition by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var scale by remember { mutableStateOf(2f) }
    var zoom_ by remember { mutableStateOf(0f) }
    var centeroid_ by remember { mutableStateOf(Offset.Zero) }
    var pan_ by remember { mutableStateOf(Offset.Zero) }
    var CustomShape by remember { mutableStateOf(GenericShape { size, layoutDirection -> }) }
    Log.d("Memory_Log_Compare", dirName)
    val context = LocalContext.current
    val path = context.filesDir.toString()
    val fileOriginal = File("$path/memo/$dirName/original.JPG")
    val fileMask = File("$path/memo/$dirName/mask.JPG")
    Image(
//        painter = painterResource(id = R.drawable.original),
        bitmap = BitmapFactory.decodeFile(fileOriginal.path).asImageBitmap(),
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
//        painter = painterResource(id = R.drawable.masked),
        bitmap = BitmapFactory.decodeFile(fileMask.path).asImageBitmap(),
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
    dirName : String,
    navController: NavController
) {
    // content
    Text(
        text = "Compare",
        modifier = Modifier.padding(all = 8.dp)
    )
    Test(dirName)
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MemoryTheme {
        Greeting("Android")
//        Test()
        SetNav()
    }
}