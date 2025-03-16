package com.example.gochat.ui.my

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.gochat.R
import com.example.gochat.config.config
import com.example.gochat.data.ApiService
import com.example.gochat.data.database.dao.UserInfoDao
import com.example.gochat.data.database.entity.UserInfo
import com.example.gochat.databinding.FragmentMyBinding
import com.example.gochat.utils.TokenManager
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.koin.android.ext.android.inject
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MyFragment : Fragment() {

    private var _binding: FragmentMyBinding? = null
    private val binding get() = _binding!!
    private val apiService: ApiService by inject() // 通过 Koin 注入 ApiService
    private val userInfoDao: UserInfoDao by inject() // 通过 Koin 注入 UserInfoDao

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initUI() {
        binding.saveButton.setOnClickListener {
            println("Save button clicked")
            // TODO: 实现保存逻辑
        }

        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed() // 临时返回，可改为导航
        }

        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                // TODO: 根据 menu 文件定义的 ID 处理菜单项点击
                else -> false
            }
        }

        // 初始化时获取用户信息
        fetchUserProfile()
    }

    private fun fetchUserProfile() {
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            val token = TokenManager.getAccessToken(requireContext()) ?: ""
            try {
                val response = apiService.getUserProfile(token)
                if (response.isSuccessful) {
                    val body = response.body()
                    val userId = TokenManager.getUserIdFromToken(token)

                    if (userId == null) {
                        Toast.makeText(requireContext(), "用户信息获取失败：无效的 token", Toast.LENGTH_SHORT).show()
                        return@launch
                    }

                    if (body != null) {
                        val userInfo = UserInfo(
                            id = userId,
                            displayName = body.displayname ?: "未设置昵称",
                            email = body.email ?: "",
                            avatarUrl = body.avatarUrl ?: "",
                            bio = body.bio ?: "这个人很懒，什么都没写",
                            gender = body.sex ?: "unspecified",
                            birthDate = body.birth?.let {
                                try {
                                    LocalDate.parse(it.substring(0, 10), DateTimeFormatter.ISO_LOCAL_DATE)
                                } catch (e: Exception) {
                                    null
                                }
                            },
                            phoneNumber = body.Phonenumber ?: "",
                            location = body.localcity ?: ""
                        )

                        // 更新 UI
                        displayUserInfo(userInfo)

                        // 更新或插入数据库
                        val existingUser = userInfoDao.getUserInfoById(userId)
                        if (existingUser != null) {
                            userInfoDao.update(userInfo)
                            println("用户信息已更新: $userInfo")
                        } else {
                            userInfoDao.insert(userInfo)
                            println("新用户信息已插入: $userInfo")
                        }
                    } else {
                        println("User data is null")
                        Toast.makeText(requireContext(), "用户信息为空", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // 处理错误响应
                    val errorBody = response.errorBody()?.string() ?: "{}"
                    val jsonObject = JSONObject(errorBody)
                    val message = jsonObject.optString("message", "未知错误")

                    when (response.code()) {
                        400 -> Toast.makeText(requireContext(), "错误: $message", Toast.LENGTH_SHORT).show()
                        500 -> Toast.makeText(requireContext(), "服务器错误，请稍后重试", Toast.LENGTH_SHORT).show()
                        else -> Toast.makeText(requireContext(), "获取用户信息失败: $message", Toast.LENGTH_SHORT).show()
                    }
                    println("Failed to fetch user profile: ${response.code()} - $message")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println("Error fetching user profile: ${e.message}")
                Toast.makeText(requireContext(), "网络错误，请检查连接", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun displayUserInfo(user: UserInfo) {
        binding.editDisplayName.setText(user.displayName)
        binding.editEmail.setText(user.email)
        binding.editBio.setText(user.bio)
        binding.editGender.setText(user.gender)
        binding.editBirthDate.setText(user.birthDate?.format(DateTimeFormatter.ISO_LOCAL_DATE))
        binding.editPhoneNumber.setText(user.phoneNumber)
        binding.editLocation.setText(user.location)

        lifecycleScope.launch {
            // 从数据库获取后端路径
            val localUser = userInfoDao.getUserInfoById(user.id)
            val storedAvatarUrl = localUser?.avatarUrl ?: user.avatarUrl // 使用数据库值，fallback 到传入值

            if (!storedAvatarUrl.isNullOrEmpty()) {
                // 拼接本地完整路径
                val localAvatarFile = File(requireContext().filesDir, storedAvatarUrl.trimStart('/'))
                // 例如：/data/user/0/com.example.gochat/files/upload/xx.jpg

                if (localAvatarFile.exists()) {
                    // 本地文件存在，直接加载
                    Glide.with(this@MyFragment)
                        .load(localAvatarFile)
                        .placeholder(R.drawable.ic_default_avatar)
                        .error(R.drawable.ic_default_avatar)
                        .into(binding.avatarImage)
                } else if (storedAvatarUrl.startsWith("/upload/")) {
                    // 本地无文件，从后端下载
                    val backendAvatarUrl = config.BACKEND_URL + storedAvatarUrl // 例如 http://127.0.0.1:8000/upload/xx.jpg
                    try {
                        val avatarFile = downloadAndSaveAvatar(backendAvatarUrl, storedAvatarUrl)
                        if (avatarFile != null) {
                            // 加载本地头像
                            Glide.with(this@MyFragment)
                                .load(avatarFile)
                                .placeholder(R.drawable.ic_default_avatar)
                                .error(R.drawable.ic_default_avatar)
                                .into(binding.avatarImage)
                        } else {
                            binding.avatarImage.setImageResource(R.drawable.ic_default_avatar)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        println("Error downloading avatar: ${e.message}")
                        binding.avatarImage.setImageResource(R.drawable.ic_default_avatar)
                    }
                } else {
                    // 不是 /upload/ 开头的路径，使用默认头像
                    binding.avatarImage.setImageResource(R.drawable.ic_default_avatar)
                }
            } else {
                // 无头像路径，使用默认头像
                binding.avatarImage.setImageResource(R.drawable.ic_default_avatar)
            }
        }
    }

    private suspend fun downloadAndSaveAvatar(url: String, relativePath: String): File? {
        return try {
            val file = Glide.with(this@MyFragment)
                .asFile()
                .load(url)
                .submit()
                .get()

            // 拼接本地保存路径，保持与后端一致
            val localFile = File(requireContext().filesDir, relativePath.trimStart('/')) // 例如 files/upload/xx.jpg
            val parentDir = localFile.parentFile
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs() // 创建目录结构
            }

            // 直接覆盖保存
            file.copyTo(localFile, overwrite = true)
            file.delete() // 删除 Glide 临时文件

            localFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}