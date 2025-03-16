//package com.example.gochat.ui.addfriend
//
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.gochat.api.Friend
//import com.example.gochat.api.FriendApi
//import com.example.gochat.api.FriendRequest
//import kotlinx.coroutines.launch
//
//class AddFriendViewModel(
//    private val friendApi: FriendApi // Koin 注入的 FriendApi
//) : ViewModel() {
//
//    private val _searchResults = MutableLiveData<List<Friend>>()
//    val searchResults: LiveData<List<Friend>> get() = _searchResults
//
//    private val _errorMessage = MutableLiveData<String>()
//    val errorMessage: LiveData<String> get() = _errorMessage
//
//    fun searchUser(username: String) {
//        viewModelScope.launch {
//            try {
//                val response = friendApi.searchUser(username)
//                if (response.isSuccessful) {
//                    _searchResults.postValue(response.body() ?: emptyList())
//                } else {
//                    _errorMessage.postValue("搜索失败：${response.code()} - ${response.message()}")
//                }
//            } catch (e: Exception) {
//                _errorMessage.postValue("网络错误：${e.message}")
//            }
//        }
//    }
//
//    fun sendFriendRequest(targetId: Long) {
//        viewModelScope.launch {
//            try {
//                val response = friendApi.sendFriendRequest(FriendRequest(targetId))
//                if (response.isSuccessful) {
//                    // 请求成功，无需额外返回值
//                } else {
//                    _errorMessage.postValue("发送好友请求失败：${response.code()} - ${response.message()}")
//                }
//            } catch (e: Exception) {
//                _errorMessage.postValue("网络错误：${e.message}")
//            }
//        }
//    }
//}