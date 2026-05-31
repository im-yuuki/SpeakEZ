package me.june8th.speakez.ui.common

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestoreException

fun Throwable.toUserMessage(defaultMessage: String): String {
    return when (this) {
        is FirebaseAuthInvalidCredentialsException -> "Email hoặc mật khẩu không đúng"
        is FirebaseAuthInvalidUserException -> "Tài khoản không tồn tại hoặc đã bị vô hiệu hóa"
        is FirebaseAuthUserCollisionException -> "Email này đã được sử dụng"
        is FirebaseAuthWeakPasswordException -> "Mật khẩu cần ít nhất 6 ký tự"
        is FirebaseNetworkException -> "Không có kết nối mạng. Vui lòng kiểm tra Internet"
        is FirebaseTooManyRequestsException -> "Bạn đã thử quá nhiều lần. Vui lòng thử lại sau"
        is FirebaseFirestoreException -> toFirestoreUserMessage(defaultMessage)
        is IllegalArgumentException, is IllegalStateException -> message ?: defaultMessage
        else -> defaultMessage
    }
}

private fun FirebaseFirestoreException.toFirestoreUserMessage(defaultMessage: String): String {
    return when (code) {
        FirebaseFirestoreException.Code.PERMISSION_DENIED -> "Bạn không có quyền thực hiện thao tác này"
        FirebaseFirestoreException.Code.UNAUTHENTICATED -> "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại"
        FirebaseFirestoreException.Code.UNAVAILABLE -> "Không thể kết nối Firebase. Vui lòng thử lại sau"
        FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> "Kết nối Firebase quá chậm. Vui lòng thử lại"
        FirebaseFirestoreException.Code.RESOURCE_EXHAUSTED -> "Hệ thống đang quá tải. Vui lòng thử lại sau"
        FirebaseFirestoreException.Code.ABORTED -> "Dữ liệu vừa thay đổi. Vui lòng thử lại"
        FirebaseFirestoreException.Code.NOT_FOUND -> "Không tìm thấy dữ liệu cần xử lý"
        FirebaseFirestoreException.Code.ALREADY_EXISTS -> "Dữ liệu này đã tồn tại"
        else -> defaultMessage
    }
}
