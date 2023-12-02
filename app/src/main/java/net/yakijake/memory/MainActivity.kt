package net.yakijake.memory

import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.yakijake.memory.ui.theme.AppDatabase
import net.yakijake.memory.ui.theme.Memo
import net.yakijake.memory.ui.theme.MemoryTheme
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 必要なフォルダを生成
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
                    SetNav()
                }
            }
        }
    }
}

@Composable
fun SetNav() {
    // ナビゲーション設定
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "topContent") {
        // トップ画面
        composable("topContent") {
            TopContent(navController = navController)
        }
        // 画像比較
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
    }
}

@Composable
fun TopContent(
    navController: NavController,
) {
    val context = LocalContext.current

    Column {
        Button(
            onClick = {
                navController.navigate("compareContent")

                CoroutineScope(Dispatchers.IO).launch {
                    // DB用意
                    val db = Room.databaseBuilder(
                        context,
                        AppDatabase::class.java, "memo-db"
                    ).build()

                    // DB全件出力
                    val memoDao = db.memoDao()
                    val memoList: List<Memo> = memoDao.getAll()
                    memoList.forEach {
                        Log.d("memoList", "uuid:${it.uuid},title:${it.title}")
                    }
                }
            }
        ) {
            Text( text = "DBログ出力" )
        }

        // スクロールできるやつ
        LazyColumn {
            // メモ領域の一番上の階層のファイルリストを取得
            val path = context.filesDir.toString()

            val dirList = File("$path/memo").list()
            dirList.forEach {
                if (File("$path/memo", it).isDirectory) {
                    Log.d("Memory_Log_FileList", it)
                }
            }

            // dirListの分だけTextを生成してくれる
            items(dirList) {
                Text(
                    text = it,
                    modifier = Modifier
                        .padding(all = 16.dp)
                        .clickable {
                            // クリックしたらcompareContent(画像比較のComposable)に遷移
                            Log.d("Memory_Log_Click", it)
                            navController.navigate("compareContent/$it")
                        }
                )
            }
        }
    }
}


@Composable
fun ShowImage(isWide : Boolean, originalBitmap : Bitmap, maskBitmap : Bitmap, modifier: Modifier = Modifier) {
    var sliderPosition by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var scale by remember { mutableStateOf(1f) }
    var centeroid_ by remember { mutableStateOf(Offset.Zero) }
    var pan_ by remember { mutableStateOf(Offset.Zero) }
    // 切り抜き型
    var CustomShape by remember { mutableStateOf(GenericShape { size, layoutDirection -> }) }

    Image(
        painter = BitmapPainter(originalBitmap.asImageBitmap()),
        contentDescription = "An Image",
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .pointerInput(Unit) {
                detectTransformGestures(true) { centeroid, pan, zoom, _ ->
                    // ジェスチャー操作を拾って画像に反映
                    scale *= zoom
                    if (scale < 1) scale = 1f
                    if (scale > 10) scale = 10f
                    offset += pan

                    pan_ = pan
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
                translationY = offset.y
            }
    )
    Image(
        painter = BitmapPainter(maskBitmap.asImageBitmap()),
        contentDescription = "An Image",
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .fillMaxHeight()
            .clip(CustomShape) // 作成したカスタムShapeで切り抜く
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationX = offset.x
                translationY = offset.y
            }
    )

    Column {
        // 画像画面でオーバーレイ的に出てくるとこ
        Text(
            text = "${offset.x} / $scale = ${(offset.x/scale)}",
            modifier = Modifier.padding(all = 8.dp)
        )
        Slider(
            value = sliderPosition,
            onValueChange = {
                sliderPosition = it
                CustomShape = GenericShape { size, layoutDirection ->
                    if (isWide) {
                        moveTo(0f, 0f)
                        lineTo(size.width*sliderPosition, 0f)
                        lineTo(size.width*sliderPosition, size.height)
                        lineTo(0f, size.height)
                    } else {
                        moveTo(0f, size.height*(1-sliderPosition))
                        lineTo(size.width, size.height*(1-sliderPosition))
                        lineTo(size.width, size.height)
                        lineTo(0f, size.height)
                    }
                }
            }
        )
    }
}

@Composable
fun CompareContent(
    dirName: String,
    navController: NavController,
) {
    // トップ画面のテキストクリックして遷移してくる
    Log.d("CompareContent","Test Compare")
    // 画像を準備
    Log.d("Memory_Log_Compare", dirName)
    val context = LocalContext.current
    val path = context.filesDir.toString()
    // 元画像読み込み
    val fileOriginal = File("$path/memo/$dirName/original.JPG")
    val beforeResizeOriginal = BitmapFactory.decodeFile(fileOriginal.path)
    // 解像度に合わせて画像をリサイズ 縦横の長い方が画面と同じ長さ
    val resizeScale:Double
    val screenHeight = LocalConfiguration.current.screenHeightDp
    val screenWidth = LocalConfiguration.current.screenWidthDp
    // ズームできるので、画面解像度よりちょっと多くする
    val resizeHosei = 1.4
    // 端末の向き判定
    val isScreenWide = screenWidth > screenHeight
    // 縦横長判定
    val isPhotoWide = beforeResizeOriginal.width > beforeResizeOriginal.height
    resizeScale = if (isScreenWide) {
        // 横向き
        screenWidth.toDouble() / beforeResizeOriginal.width
    } else {
        // 縦向き
        screenHeight.toDouble() / beforeResizeOriginal.height
    }
    // リサイズ
    val afterResizeOriginal = Bitmap.createScaledBitmap(
        beforeResizeOriginal,
        (beforeResizeOriginal.width * resizeScale*resizeHosei).toInt(),
        (beforeResizeOriginal.height*resizeScale*resizeHosei).toInt(),
        true
    )
    beforeResizeOriginal.recycle()
    // マスク画像も同様
    val fileMask = File("$path/memo/$dirName/mask.JPG")
    val beforeResizeMask = BitmapFactory.decodeFile(fileMask.path)
    // リサイズ
    val afterResizeMask = Bitmap.createScaledBitmap(
        beforeResizeMask,
        (beforeResizeMask.width*resizeScale*resizeHosei).toInt(),
        (beforeResizeMask.height*resizeScale*resizeHosei).toInt(),
        true
    )
    // メモリ解放
    beforeResizeMask.recycle()
    Log.d("Memory_Log_Resize", "All Resized")
    // 画像表示
    ShowImage(isPhotoWide, afterResizeOriginal, afterResizeMask)
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MemoryTheme {
        SetNav()
    }
}