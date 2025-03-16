//package com.example.gochat.viewmodel
//
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.gochat.api.Friend
//import kotlinx.coroutines.launch
//
//class FriendListViewModel(
//    private val friendRepository: FriendRepository // 通过 Koin 注入
//) : ViewModel() {
//
//    private val _friendList = MutableLiveData<List<Friend>>()
//    val friendList: LiveData<List<Friend>> get() = _friendList
//
//    private val _errorMessage = MutableLiveData<String>()
//    val errorMessage: LiveData<String> get() = _errorMessage
//
//    fun loadFriends() {
//        viewModelScope.launch {
//            try {
//                val friends = friendRepository.fetchFriendList()
//                _friendList.postValue(friends)
//            } catch (e: Exception) {
//                _errorMessage.postValue("加载好友列表失败：${e.message}")
//            }
//        }
//    }
//}