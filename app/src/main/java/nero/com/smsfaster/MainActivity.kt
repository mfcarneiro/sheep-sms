package nero.com.smsfaster

import android.Manifest
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.telephony.SmsManager
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.lang.Exception

class MainActivity : AppCompatActivity(),
    TextWatcher,
    EasyPermissions.PermissionCallbacks {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        et_message.addTextChangedListener(this)
    }

    override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
        tv_counter.text = String.format("%d /%s", text.length, getString(R.string.max))
    }

    private companion object {
        const val SMS_AND_PHONE_STATE_REQUEST_CODE = 2256
    }

    override fun afterTextChanged(s: Editable?) {}
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // Send the user request to API @EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        var title = getString(R.string.title_needed_permission)
        val rationale: String
        val toastContentId: Int
        val permissions = mutableListOf<String>() // Returns a new empty mutable Array

        // Get the messages of rationale and toast, title and the denied permissions

        if (!EasyPermissions.hasPermissions(this, Manifest.permission.SEND_SMS)
            && !EasyPermissions.hasPermissions(this, Manifest.permission.READ_PHONE_STATE)
        ) {

            title = getString(R.string.title_needed_permission)
            rationale = getString(R.string.rationale_sms_phone_state_permissions)
            toastContentId = (R.string.toast_needed_permissions)
            permissions.add(Manifest.permission.SEND_SMS)
            permissions.add(Manifest.permission.READ_PHONE_STATE)

        } else if (!EasyPermissions.hasPermissions(this, Manifest.permission.SEND_SMS)) {
            rationale = getString(R.string.rationale_needed_sms_permission)
            toastContentId = R.string.toast_needed_sms_permission
            permissions.add(Manifest.permission.SEND_SMS)
        } else {
            rationale = getString(R.string.rationale_needed_phone_permission)
            toastContentId = R.string.toast_needed_phone_permission
            permissions.add(Manifest.permission.READ_PHONE_STATE)
        }

        if (!EasyPermissions.somePermissionPermanentlyDenied(this, permissions)) {
            AppSettingsDialog
                .Builder(this)
                .setTitle(title)
                .setRationale(rationale)
                .build()
                .show()
        } else {
            makeToast(toastContentId)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            var toastContentId = R.string.toast_perms_granted

            if (!EasyPermissions.hasPermissions(this, Manifest.permission.SEND_SMS)
                && !EasyPermissions.hasPermissions(this, Manifest.permission.READ_PHONE_STATE)
            ) {
                toastContentId = R.string.toast_perms_not_yet_granted
            } else if (!EasyPermissions.hasPermissions(this, Manifest.permission.SEND_SMS)) {
                toastContentId = R.string.toast_perm_sms_not_yet_granted
            } else if (!EasyPermissions.hasPermissions(this, Manifest.permission.READ_PHONE_STATE)) {
                toastContentId = R.string.toast_perm_phone_not_yet_granted
            }

            makeToast(toastContentId)
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        // Permission granted, invoke the sendSMS() method
        if (EasyPermissions.hasPermissions(this, Manifest.permission.READ_PHONE_STATE)) {
            sendSMS()
        }
    }

    /*
    * Click listener of SMS button
    * Initializing the permissions
     */
    fun sendSMS(view: View) {
        EasyPermissions.requestPermissions(
            this,
            getString(R.string.rationale_sms_phone_state_permissions),
            SMS_AND_PHONE_STATE_REQUEST_CODE,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_PHONE_STATE
        )
    }

    private fun sendSMS() {
        try {
            val number = String.format("+%s%s%s", et_ddi.text, et_ddd.text, et_number.text)
            val message = et_message.text.toString()

            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(number, null, message, null, null)

            makeToast(R.string.sms_sent_successfully)
            clearTextMessage()
        } catch (exception: Exception) {
            exception.printStackTrace()
            makeToast(R.string.sms_error)
        }

    }

    fun clearTextMessage(view: View) {
        et_message.text.clear()
    }

    private fun clearTextMessage() {
        et_message.text.clear()
    }

    private fun makeToast(messageId: Int) {
        Toast
            .makeText(this, getString(messageId), Toast.LENGTH_LONG)
            .show()
    }

}
