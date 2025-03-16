//package com.example.gochat.viewmodel
//
//import android.R
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.gochat.api.Message
//import com.example.gochat.api.MessageApi
//import com.google.gson.Gson
//import kotlinx.coroutines.launch
//import okhttp3.OkHttpClient
//import okhttp3.Request
//import okhttp3.Response
//import okhttp3.WebSocket
//import okhttp3.WebSocketListener
//import okio.ByteString
//
//class ChatViewModel(
//    private val messageApi: MessageApi, // 通过 Koin 注入
//) : ViewModel() {
//
//    private val _messages = MutableLiveData<List<Message>>()
//    val messages: LiveData<List<Message>> get() = _messages
//
//    private val _errorMessage = MutableLiveData<String>()
//    val errorMessage: LiveData<String> get() = _errorMessage
//
//    private val client = OkHttpClient()
//    private var webSocket: WebSocket? = null
//
//    fun loadMessages(friendId: Long) {
//        viewModelScope.launch {
//            try {
//                val response = messageApi.getMessages(friendId)
//                if (response.isSuccessful) {
//                    _messages.postValue(response.body() ?: emptyList())
//                } else {
//                    _errorMessage.postValue("加载消息失败：${response.code()} - ${response.message()}")
//                }
//            } catch (e: Exception) {
//                _errorMessage.postValue("网络错误：${e.message}")
//            }
//        }
//    }
//
//    fun sendMessage(friendId: Long, content: String,currentUserId: String) {
//        viewModelScope.launch {
//            try {
//                val message = Message(0, currentUserId, friendId, content, System.currentTimeMillis())
//                val response = messageApi.sendMessage(message)
//                if (response.isSuccessful) {
//                    // 通过 WebSocket 发送消息（可选，后端可能已广播）
//                    webSocket?.send(Gson().toJson(message))
//                    val currentList = _messages.value.orEmpty().toMutableList()
//                    currentList.add(message) // 本地更新
//                    _messages.postValue(currentList)
//                } else {
//                    _errorMessage.postValue("发送消息失败：${response.code()} - ${response.message()}")
//                }
//            } catch (e: Exception) {
//                _errorMessage.postValue("网络错误：${e.message}")
//            }
//        }
//    }
//
//    fun startWebSocket(friendId: Long) {
//        val request = Request.Builder()
//            .url("ws://192.168.137.1:8000/ws/chat/$friendId") // 替换为实际 WebSocket URL
//            .build()
//        webSocket = client.newWebSocket(request, object : WebSocketListener() {
//            override fun onOpen(webSocket: WebSocket, response: Response) {
//                // WebSocket 连接成功
//            }
//
//            override fun onMessage(webSocket: WebSocket, text: String) {
//                // 接收到新消息
//                val message = Gson().fromJson(text, Message::class.java)
//                val currentList = _messages.value.orEmpty().toMutableList()
//                currentList.add(message)
//                _messages.postValue(currentList)
//            }
//
//            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
//                // 处理二进制消息（可选）
//            }
//
//            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
//                webSocket.close(1000, null)
//            }
//
//            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
//                _errorMessage.postValue("WebSocket 连接失败：${t.message}")
//            }
//        })
//    }
//
//    override fun onCleared() {
//        webSocket?.close(1000, "ViewModel cleared")
//        client.dispatcher.executorService.shutdown()
//        super.onCleared()
//    }
//}