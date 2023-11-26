package net.yakijake.memory

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import net.yakijake.memory.ui.theme.MemoryTheme
import java.io.BufferedInputStream
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Paths


class ImportActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        when {
            intent?.action == Intent.ACTION_SEND_MULTIPLE
                    && intent.type?.startsWith("image/") == true -> {
                handleSendMultipleImages(intent) // Handle multiple images being sent
            }
            else -> {
                // Handle other intents, such as being started from the home screen
            }
        }
//        Log.d("Memory_Log",filesDir.toString())
//        val path = filesDir.toString()
//
//        Log.d("Memory_Log_DirCheck","checking")
//
//        if (!File(path, "db").exists()) {
//            Files.createDirectory(Paths.get("$path/db"))
//            Log.d("Memory_Log_DirCheck","created db")
//        }
//        if (!File(path, "memo").exists()) {
//            Files.createDirectory(Paths.get("$path/memo"))
//            Log.d("Memory_Log_DirCheck","created memo")
//        }
//
//        setContent {
//            MemoryTheme {
//                // A surface container using the 'background' color from the theme
//                Surface(
//                    modifier = Modifier.fillMaxSize(),
//                    color = MaterialTheme.colorScheme.background
//                ) {
////                    Greeting("Android")
////                    Test()
//                    PhotoImport()
//                }
//            }
//        }
    }

    private fun handleSendMultipleImages(intent: Intent) {
        intent.getParcelableArrayListExtra<Parcelable>(Intent.EXTRA_STREAM)?.let {
            // Update UI to reflect multiple images being shared
            it.forEach {
                Log.d("ImportActivity",it.toString())


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
                        PhotoImport(it)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoImport(parcelableList : ArrayList<Parcelable>) {
    var count by remember { mutableStateOf(0) }
    Log.d("PhotoImport", "ArrayLength : ${parcelableList.size}")
    val contentUri = parcelableList[count].toString()
    Log.d("PhotoImport","contentUri : $contentUri")
    val context = LocalContext.current
    val stream : InputStream = context.getContentResolver().openInputStream(contentUri.toUri())!!
    val importImage = BitmapFactory.decodeStream(BufferedInputStream(stream))
    // 画像を準備
//    val context = LocalContext.current
//    val path = context.filesDir.toString()
    // 元画像読み込み
    val importFile = File(contentUri)
//    val importImage = BitmapFactory.decodeFile(importFile.path)

    val inputValue = rememberSaveable { mutableStateOf("ディレクトリ名") }

    val saveState = remember { MutableList(parcelableList.size){""} }

    Column {
        Image(
            painter = BitmapPainter(importImage.asImageBitmap()),
            contentDescription = "An Image",
            contentScale = ContentScale.Fit,
            modifier = Modifier
        )

        Button(
            onClick = {
                Log.d("PhotoImport", inputValue.value)
                saveState.forEach {
                    Log.d("PhotoImport", "saveState : $it")
                }
                if (parcelableList.size > count+1) {
                    // スキップ処理
                    saveState[count] = ""
                    count++
                } else {
                    Log.d("PhotoImport", "これが最後の画像")
                }
            }
        ) {
            Text( text = "スキップ/次の画像" )
        }

        TextField(
            value = inputValue.value,
            onValueChange = { inputValue.value = it },// ラムダ式の引数はitで受け取れる
            label = { /*TODO*/ },
            modifier = Modifier.padding(16.dp)
        )

        Button(
            onClick = {
                if (saveState.contains("original.JPG")) {
                    Log.d("PhotoImport", "originalが既にあります")
                } else {
                    saveState[count] = "original.JPG"
                    if (parcelableList.size > count+1) {
                        count++
                    } else {
                        Log.d("PhotoImport", "これが最後の画像")
                        Toast.makeText(context, "これが最後の画像", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        ) {
            Text( text = "元画像保存" )
        }

        Button(
            onClick = {
                if (saveState.contains("mask.JPG")) {
                    Log.d("PhotoImport", "maskが既にあります")
                } else {
                    saveState[count] = "mask.JPG"
                    if (parcelableList.size > count+1) {
                        count++
                    } else {
                        Log.d("PhotoImport", "これが最後の画像")
                    }
                }
            }
        ) {
            Text( text = "加工画像保存" )
        }

        Button(
            onClick = {
                Log.d("PhotoImport", inputValue.value)
                saveState.forEach {
                    if (it == "") {
                        Log.d("PhotoImport", "saveState : からっぽ。")
                    } else {
                        Log.d("PhotoImport", "saveState : $it")
                    }
                }

                if (parcelableList.size <= count+1) {
                    Log.d("PhotoImport", "これが最後の画像 保存します")

                    Log.d("PhotoImport","DirName checking")

                    val path = "${context.filesDir}/memo"

                    if (File(path, inputValue.value).exists()) {
                        Log.d("PhotoImport","フォルダが既に存在する")
                    } else {
                        Files.createDirectory(Paths.get("$path/${inputValue.value}"))
                        Log.d("PhotoImport","created memo/${inputValue.value}")

                        for (i in 1..parcelableList.size) {
                            val cnt = i-1

                            Log.d("PhotoImportSave", "count: $cnt")

                            if (saveState[cnt] != "") {
                                val contentUri2 = parcelableList[cnt].toString()

                                val saveStream : InputStream = context.getContentResolver().openInputStream(contentUri2.toUri())!!

                                val savePath = "$path/${inputValue.value}/${saveState[cnt]}"
                                File(savePath).outputStream().use {
                                    saveStream.copyTo(it)
                                }

                                Log.d("PhotoImport", "saveState : からっぽ。")
                                // 保存
                            }

                        }

                        Toast.makeText(context, "全て保存しました。", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        ) {
            Text( text = "最終保存" )
        }

    }
}