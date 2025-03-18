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
import com.example.gochat.api.Respons
import com.example.gochat.api.UserProfileResponse
import com.example.gochat.config.config
import com.example.gochat.data.ApiService
import com.example.gochat.data.database.dao.UserInfoDao
import com.example.gochat.data.database.entity.UserInfo
import com.example.gochat.data.database.insertOrUpdate
import com.example.gochat.databinding.FragmentMyBinding
import com.example.gochat.utils.TokenManager
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.koin.android.ext.android.inject
import retrofit2.Response
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.io.File

class MyFragment : Fragment() {

    private var _binding: FragmentMyBinding? = null
    private val binding get() = _binding!!
    private val apiService: ApiService by inject()
    private val userInfoDao: UserInfoDao by inject()
    private var isEditMode = false

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
        fetchUserProfile()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initUI() {
        // 初始状态为只读
        setEditMode(false)

        // 点击按钮切换编辑模式
        binding.saveButton.setOnClickListener {
            if (isEditMode) {
                saveUserProfile()
            } else {
                setEditMode(true)
                binding.saveButton.text = "保存"
                Toast.makeText(requireContext(), "进入编辑模式", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setEditMode(enabled: Boolean) {
        isEditMode = enabled
        binding.editDisplayName.isEnabled = enabled
        binding.editEmail.isEnabled = enabled
        binding.editBio.isEnabled = enabled
        binding.editGender.isEnabled = enabled
        binding.editBirthDate.isEnabled = enabled
        binding.editPhoneNumber.isEnabled = enabled
        binding.editLocation.isEnabled = enabled
        binding.avatarImage.isClickable = enabled // 头像在编辑模式下可点击
        binding.saveButton.text = if (enabled) "保存" else "编辑"
    }

    private fun fetchUserProfile() {
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            val token = TokenManager.getAccessToken(requireContext())
            val userId = TokenManager.getUserId(requireContext())

            if (token == null || userId == 0) {
                Toast.makeText(requireContext(), "用户信息获取失败：未登录或 ID 无效", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
                return@launch
            }

            try {
                val cachedUser = userInfoDao.getUserInfoById(userId)
                if (cachedUser != null) {
                    displayUserInfo(cachedUser)
                }

                val response: Response<UserProfileResponse> = apiService.getUserProfile("$token")
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        val userInfo = UserInfo(
                            id = userId,
                            displayName = body.displayname.ifEmpty { "未设置昵称" },
                            email = body.email,
                            avatarUrl = body.avatarUrl,
                            bio = body.bio.ifEmpty { "这个人很懒，什么都没写" },
                            gender = body.sex.ifEmpty { "未指定" },
                            birthDate = body.birth.let { birthStr ->
                                try {
                                    LocalDate.parse(birthStr, DateTimeFormatter.ISO_LOCAL_DATE)
                                } catch (e: Exception) {
                                    null
                                }
                            },
                            phoneNumber = body.Phonenumber,
                            location = body.localcity,
                        )
                        displayUserInfo(userInfo)
                        userInfoDao.insertOrUpdate(userInfo)
                    } else {
                        Toast.makeText(requireContext(), "用户信息为空", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "{}"
                    val jsonObject = JSONObject(errorBody)
                    val message = jsonObject.optString("message", "未知错误")
                    Toast.makeText(requireContext(), "获取用户信息失败: $message", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "网络错误，请检查连接", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun saveUserProfile() {
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            val token = TokenManager.getAccessToken(requireContext())
            val userId = TokenManager.getUserId(requireContext())

            if (token == null || userId == 0) {
                Toast.makeText(requireContext(), "保存失败：未登录或 ID 无效", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
                return@launch
            }

            val updatedUserInfo = UserInfo(
                id = userId,
                displayName = binding.editDisplayName.text.toString().ifEmpty { "未设置昵称" },
                email = binding.editEmail.text.toString(),
                avatarUrl = userInfoDao.getUserInfoById(userId)?.avatarUrl ?: "",
                bio = binding.editBio.text.toString().ifEmpty { "这个人很懒，什么都没写" },
                gender = binding.editGender.text.toString().ifEmpty { "未指定" },
                birthDate = try {
                    LocalDate.parse(binding.editBirthDate.text.toString(), DateTimeFormatter.ISO_LOCAL_DATE)
                } catch (e: Exception) {
                    null
                },
                phoneNumber = binding.editPhoneNumber.text.toString(),
                location = binding.editLocation.text.toString(),
            )

            try {
                val response: Response<Respons> = apiService.updateUserProfile("Bearer $token", updatedUserInfo)
                if (response.isSuccessful) {
                    userInfoDao.insertOrUpdate(updatedUserInfo)
                    Toast.makeText(requireContext(), "保存成功", Toast.LENGTH_SHORT).show()
                    setEditMode(false)
                    binding.saveButton.text = "编辑" // 保存后恢复按钮文本
                } else {
                    val errorBody = response.errorBody()?.string() ?: "{}"
                    val jsonObject = JSONObject(errorBody)
                    val message = jsonObject.optString("message", "未知错误")
                    Toast.makeText(requireContext(), "保存失败: $message", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "网络错误，请稍后重试", Toast.LENGTH_SHORT).show()
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
        binding.editBirthDate.setText(user.birthDate?.format(DateTimeFormatter.ISO_LOCAL_DATE) ?: "未设置")
        binding.editPhoneNumber.setText(user.phoneNumber)
        binding.editLocation.setText(user.location)

        lifecycleScope.launch {
            val storedAvatarUrl = user.avatarUrl
            if (!storedAvatarUrl.isNullOrEmpty()) {
                val localAvatarFile = File(requireContext().filesDir, storedAvatarUrl.trimStart('/'))
                if (localAvatarFile.exists()) {
                    Glide.with(this@MyFragment)
                        .load(localAvatarFile)
                        .placeholder(R.drawable.ic_default_avatar)
                        .error(R.drawable.ic_default_avatar)
                        .into(binding.avatarImage)
                } else if (storedAvatarUrl.startsWith("/upload/")) {
                    val backendAvatarUrl = config.BACKEND_URL + storedAvatarUrl
                    val avatarFile = downloadAndSaveAvatar(backendAvatarUrl, storedAvatarUrl)
                    if (avatarFile != null) {
                        Glide.with(this@MyFragment)
                            .load(avatarFile)
                            .placeholder(R.drawable.ic_default_avatar)
                            .error(R.drawable.ic_default_avatar)
                            .into(binding.avatarImage)
                    } else {
                        binding.avatarImage.setImageResource(R.drawable.ic_default_avatar)
                    }
                } else {
                    binding.avatarImage.setImageResource(R.drawable.ic_default_avatar)
                }
            } else {
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

            val localFile = File(requireContext().filesDir, relativePath.trimStart('/'))
            val parentDir = localFile.parentFile
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs()
            }
            file.copyTo(localFile, overwrite = true)
            file.delete()
            localFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}