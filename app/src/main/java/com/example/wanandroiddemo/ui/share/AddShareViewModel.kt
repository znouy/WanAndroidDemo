package com.example.wanandroiddemo.ui.share

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wanandroiddemo.data.repository.ShareRepository
import com.example.wanandroiddemo.ui.common.delegate.MessageDelegate
import com.example.wanandroiddemo.util.ext.coRunCatching
import com.example.wanandroiddemo.util.ext.isValidHttpUrl
import com.example.wanandroiddemo.util.ext.logOnFailure
import com.example.wanandroiddemo.util.ext.mapNetworkException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

sealed interface AddShareUiEvent {
    object Success : AddShareUiEvent
}

/**
 *  输入框的错误状态（空代表无错误）
 */
data class ShareFormState(
    val titleError: String? = null,
    val linkError: String? = null
)

@HiltViewModel
class AddShareViewModel @Inject constructor(
    private val repository: ShareRepository,
    private val messageDelegate: MessageDelegate
) : ViewModel(), MessageDelegate by messageDelegate {

    //  物理层防抖：动作加载中（用于控制确定按钮是否可点击）
    val isSubmitLoading = MutableStateFlow(false)

    // 声明分享事件
    private val _uiEvent = Channel<AddShareUiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()
    //  声明表单状态流
    private val _formState = MutableStateFlow(ShareFormState())
    val formState: StateFlow<ShareFormState> = _formState.asStateFlow()

    /**
     * 提交分享任务
     */
    fun shareArticle(title: String, link: String) {
        val titleError = if (title.isEmpty()) "标题不能为空哦" else null
        val linkError = when {
            link.isEmpty() -> "网址不能为空哦"
            !link.isValidHttpUrl() -> "网址请以 http:// 或 https:// 开头"
            else -> null
        }
        //直接将校验计算出来的结果同步给状态流
        _formState.value = ShareFormState(titleError, linkError)
        //  如果校验未通过，直接拦截，不发起网络请求
        if (titleError != null || linkError != null) return


        viewModelScope.launch {
            Timber.d("👉 [AddShareVM] 发起分享文章业务流程...")

            coRunCatching(isSubmitLoading) {
                repository.shareArticle(title, link)
            }.logOnFailure("分享失败")
                .mapNetworkException()
                .onSuccess {
                    Timber.d("✅ [AddShareVM] 业务处理成功！")
                    _uiEvent.send(AddShareUiEvent.Success)
                    emitMessage("分享成功！")
                }
                .onFailure { exception ->
                    emitMessage(exception.message ?: "分享文章失败")
                }
        }
    }
}