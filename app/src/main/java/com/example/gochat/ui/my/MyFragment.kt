package com.example.gochat.ui.my

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.gochat.R
import com.example.gochat.data.database.entity.UserInfo
import com.example.gochat.databinding.FragmentMyBinding
import com.example.gochat.viewmodel.MyViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MyFragment : Fragment() {

    private var _binding: FragmentMyBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MyViewModel by viewModel()
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
        observeViewModel()

        // 立即显示本地数据
        viewModel.userInfo.value?.let { displayUserInfo(it) }

        // 异步获取最新数据
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.fetchUserProfile()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initUI() {
        setEditMode(false)
        binding.saveButton.setOnClickListener {
            if (isEditMode) {
                val updatedUserInfo = getUserInfoFromUI()
                viewModel.saveUserProfile(updatedUserInfo)
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
        binding.avatarImage.isClickable = enabled
        binding.saveButton.text = if (enabled) "保存" else "编辑"
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.userInfo.collect { userInfo ->
                Log.d("MyFragment", "UserInfo updated: $userInfo")
                userInfo?.let { displayUserInfo(it) }
            }
        }
        lifecycleScope.launch {
            viewModel.loading.collect { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }
        lifecycleScope.launch {
            viewModel.error.collect { message ->
                message?.let {
                    launch(Dispatchers.Main) { // 确保 Toast 在主线程显示
                        Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        lifecycleScope.launch {
            viewModel.saveSuccess.collect { success ->
                if (success) {
                    setEditMode(false)
                    binding.saveButton.text = "编辑"
                    Toast.makeText(requireContext(), "保存成功", Toast.LENGTH_SHORT).show()
                }
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

        // 处理头像
        val storedAvatarUrl = user.avatarUrl
        Log.d("MyFragment", "Stored Avatar URL: '$storedAvatarUrl'")
        if (!storedAvatarUrl.isNullOrEmpty()) {
            val localAvatarFile = File(requireContext().filesDir, storedAvatarUrl.trimStart('/'))
            Log.d("MyFragment", "Local file path: $localAvatarFile, exists: ${localAvatarFile.exists()}")
            if (localAvatarFile.exists()) {
                Log.d("MyFragment", "Loading from local file")
                Glide.with(this@MyFragment)
                    .load(localAvatarFile)
                    .placeholder(R.drawable.ic_default_avatar)
                    .error(R.drawable.ic_default_avatar)
                    .into(binding.avatarImage)
            } else if (storedAvatarUrl.startsWith("/upload/")) {
                Log.d("MyFragment", "Triggering network request for avatar")
                // 异步下载头像
                lifecycleScope.launch(Dispatchers.IO) {
                    val backendAvatarUrl = "${viewModel.backendUrl.trimEnd('/')}${storedAvatarUrl}"
                    Log.d("MyFragment", "Backend URL: $backendAvatarUrl")
                    val avatarFile = viewModel.downloadAndSaveAvatar(backendAvatarUrl, storedAvatarUrl)
                    launch(Dispatchers.Main) { // 切换回主线程更新 UI
                        if (avatarFile != null) {
                            Log.d("MyFragment", "Loading downloaded avatar: $avatarFile")
                            Glide.with(this@MyFragment)
                                .load(avatarFile)
                                .placeholder(R.drawable.ic_default_avatar)
                                .error(R.drawable.ic_default_avatar)
                                .into(binding.avatarImage)
                        } else {
                            Log.w("MyFragment", "Download failed, using default avatar")
                            binding.avatarImage.setImageResource(R.drawable.ic_default_avatar)
                        }
                    }
                }
            } else {
                Log.d("MyFragment", "URL does not start with '/upload/', using default avatar")
                binding.avatarImage.setImageResource(R.drawable.ic_default_avatar)
            }
        } else {
            Log.d("MyFragment", "URL is null or empty, using default avatar")
            binding.avatarImage.setImageResource(R.drawable.ic_default_avatar)
        }
    }

    private fun getUserInfoFromUI(): UserInfo {
        return UserInfo(
            id = viewModel.getUserId(),
            displayName = binding.editDisplayName.text.toString().ifEmpty { "未设置昵称" },
            email = binding.editEmail.text.toString(),
            avatarUrl = viewModel.getCurrentAvatarUrl() ?: "",
            bio = binding.editBio.text.toString().ifEmpty { "这个人很懒，什么都没写" },
            gender = binding.editGender.text.toString().ifEmpty { "unspecified" },
            birthDate = try {
                LocalDate.parse(binding.editBirthDate.text.toString(), DateTimeFormatter.ISO_LOCAL_DATE)
            } catch (e: Exception) {
                null
            },
            phoneNumber = binding.editPhoneNumber.text.toString(),
            location = binding.editLocation.text.toString(),
            lastLoginTime = java.time.Instant.now().toString()
        )
    }
}