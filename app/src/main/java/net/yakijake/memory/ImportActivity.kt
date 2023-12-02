package net.yakijake.memory

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Looper
import android.os.Parcelable
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
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
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.yakijake.memory.ui.theme.AppDatabase
import net.yakijake.memory.ui.theme.Memo
import net.yakijake.memory.ui.theme.MemoryTheme
import java.io.BufferedInputStream
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.util.UUID


class ImportActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 共有されてきた中身を処理
        when {
            intent?.action == Intent.ACTION_SEND_MULTIPLE
                    && intent.type?.startsWith("image/") == true -> {
                handleSendMultipleImages(intent) // Handle multiple images being sent
            }
            else -> {
                // Handle other intents, such as being started from the home screen
            }
        }
    }

    private fun handleSendMultipleImages(intent: Intent) {
        intent.getParcelableArrayListExtra<Parcelable>(Intent.EXTRA_STREAM)?.let {
            // Update UI to reflect multiple images being shared
            // Googleフォトから共有された場合はcontent://の形で入っている
            it.forEach {
                Log.d("ImportActivity",it.toString())
            }

            setContent {
                MemoryTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        PhotoImport(it)
                    }
                }
            }
        }
    }
}

// 一部のコンポーネントはこれが必要
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoImport(parcelableList : ArrayList<Parcelable>) {
    // countが更新されるたびに処理が走る
    var count by remember { mutableStateOf(0) }
    Log.d("PhotoImport", "ArrayLength : ${parcelableList.size}")
    val contentUri = parcelableList[count].toString()
    Log.d("PhotoImport","contentUri : $contentUri")
    val context = LocalContext.current
    // content://の形式から画像ファイル自体を取得
    val stream : InputStream = context.getContentResolver().openInputStream(contentUri.toUri())!!
    val importImage = BitmapFactory.decodeStream(BufferedInputStream(stream))
    // 保存名
    val inputValue = rememberSaveable { mutableStateOf("ディレクトリ名") }
    // 指定した保存方法を覚えるList
    val saveState = remember { MutableList(parcelableList.size){""} }

    Column {
        // 画像を表示
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
            onValueChange = { inputValue.value = it },// itはTextFieldに入力された文字列
            label = { /*TODO*/ },
            modifier = Modifier.padding(16.dp)
        )

        // 保存方法指定ボタン
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

        // 書き込み処理ボタン
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

                    val memoName = inputValue.value

                    // ディレクトリ名は生成したuuid
                    val uuid = UUID.randomUUID().toString()
                    Log.d("PhotoImport", "generated uuid : $uuid")

                    if (File(path, uuid).exists()) {
                        Log.d("PhotoImport","uuid重複")
                    } else {
                        // ディレクトリ作成
                        Files.createDirectory(Paths.get("$path/$uuid"))
                        Log.d("PhotoImport","created memo/$uuid")

                        // 選択した画像を全て保存
                        for (i in 1..parcelableList.size) {
                            val cnt = i-1

                            Log.d("PhotoImportSave", "count: $cnt")

                            // スキップしたやつは保存しない
                            if (saveState[cnt] != "") {
                                val contentUri2 = parcelableList[cnt].toString()
                                // ファイル自体を取得
                                val saveStream : InputStream = context.getContentResolver().openInputStream(contentUri2.toUri())!!
                                val savePath = "$path/$uuid/${saveState[cnt]}"
                                File(savePath).outputStream().use {
                                    // 自アプリの領域にコピー
                                    saveStream.copyTo(it)
                                }
                                Log.d("PhotoImport", "saveState : からっぽ。")
                            }

                        }

                        Toast.makeText(context, "ファイル書込み完了", Toast.LENGTH_SHORT).show()

                        Log.d("PhotoImport", "CoroutineScope前")

                        CoroutineScope(Dispatchers.IO).launch {
                            // DB用意
                            val db = Room.databaseBuilder(
                                context,
                                AppDatabase::class.java, "memo-db"
                            ).build()

                            // DBログ出力
                            val memoDao = db.memoDao()

                            val memoList: List<Memo> = memoDao.getAll()
                            memoList.forEach {
                                Log.d("memoList", "uuid:${it.uuid},title:${it.title}")
                            }

                            // DB書き込み
                            val query = Memo(uuid,memoName,"","/","","")
                            memoDao.insertAll(query)

                            Looper.prepare()
                            Toast.makeText(context, "DB書き込み完了。", Toast.LENGTH_SHORT).show()
                            Log.d("PhotoImport", "DB書き込み完了")
                        }
                        Log.d("PhotoImport", "CoroutineScope後")
                    }
                }
            }
        ) {
            Text( text = "最終保存" )
        }

    }
}