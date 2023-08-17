# Alarm

App báo thức

![z4610645396800_1d1d8aa12ec9e85e9effdb6dcf84f7aa.jpg](Alarm%20b797c4d0c7694924a529122dfa60fdf2/z4610645396800_1d1d8aa12ec9e85e9effdb6dcf84f7aa.jpg)

![z4610645394768_f3c0755105744080665490c5aa627cb6.jpg](Alarm%20b797c4d0c7694924a529122dfa60fdf2/z4610645394768_f3c0755105744080665490c5aa627cb6.jpg)

![z4610645409589_75513f1edb6c5db970caa0e29e49d981.jpg](Alarm%20b797c4d0c7694924a529122dfa60fdf2/z4610645409589_75513f1edb6c5db970caa0e29e49d981.jpg)

# Sử dụng

- Alarm manager
- Room database
- Notification
- Service
- Broadcast receiver
- Material UI 3
- Jetpack compose

# Alarm manager

- Lên lịch để gọi một intent như service hoặc broadcast receiver

# Vấn đề gặp phải

- Để làm thiết bị sáng màn hình khi có thông báo báo thức làm thế nào ?
    - Sử dụng loại alarm là **[RTC_WAKEUP](https://developer.android.com/reference/android/app/AlarmManager#RTC_WAKEUP)**
- Nhiều hãng thiết bị của Trung Quốc chặn foreground service thì làm thế nào ?
    - Những hãng như Xiaomi hay Oppo cần xin quyền Auto run
    - Run activity intent với các action theo từng hãng
    
    ```java
    /***
     * Xiaomi
     */
    private final String BRAND_XIAOMI = "xiaomi";
    private String PACKAGE_XIAOMI_MAIN = "com.miui.securitycenter";
    private String PACKAGE_XIAOMI_COMPONENT = "com.miui.permcenter.autostart.AutoStartManagementActivity";
    
    /***
     * Letv
     */
    private final String BRAND_LETV = "letv";
    private String PACKAGE_LETV_MAIN = "com.letv.android.letvsafe";
    private String PACKAGE_LETV_COMPONENT = "com.letv.android.letvsafe.AutobootManageActivity";
    
    /***
     * ASUS ROG
     */
    private final String BRAND_ASUS = "asus";
    private String PACKAGE_ASUS_MAIN = "com.asus.mobilemanager";
    private String PACKAGE_ASUS_COMPONENT = "com.asus.mobilemanager.powersaver.PowerSaverSettings";
    
    /***
     * Honor
     */
    private final String BRAND_HONOR = "honor";
    private String PACKAGE_HONOR_MAIN = "com.huawei.systemmanager";
    private String PACKAGE_HONOR_COMPONENT = "com.huawei.systemmanager.optimize.process.ProtectActivity";
    
    /**
     * Oppo
     */
    private final String BRAND_OPPO = "oppo";
    private String PACKAGE_OPPO_MAIN = "com.coloros.safecenter";
    private String PACKAGE_OPPO_FALLBACK = "com.oppo.safe";
    private String PACKAGE_OPPO_COMPONENT = "com.coloros.safecenter.permission.startup.StartupAppListActivity";
    private String PACKAGE_OPPO_COMPONENT_FALLBACK = "com.oppo.safe.permission.startup.StartupAppListActivity";
    private String PACKAGE_OPPO_COMPONENT_FALLBACK_A = "com.coloros.safecenter.startupapp.StartupAppListActivity";
    
    /**
     * Vivo
     */
    
    private final String BRAND_VIVO = "vivo";
    private String PACKAGE_VIVO_MAIN = "com.iqoo.secure";
    private String PACKAGE_VIVO_FALLBACK = "com.vivo.perm;issionmanager";
    private String PACKAGE_VIVO_COMPONENT = "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity";
    private String PACKAGE_VIVO_COMPONENT_FALLBACK = "com.vivo.permissionmanager.activity.BgStartUpManagerActivity";
    private String PACKAGE_VIVO_COMPONENT_FALLBACK_A = "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager";
    
    /**
     * Nokia
     */
    
    private final String BRAND_NOKIA = "nokia";
    private String PACKAGE_NOKIA_MAIN = "com.evenwell.powersaving.g3";
    private String PACKAGE_NOKIA_COMPONENT = "com.evenwell.powersaving.g3.exception.PowerSaverExceptionActivity";
    ```
    
- Tùy hãng thiết bị sẽ có các nhạc chuông mặc định khác nhau. Vậy để lấy được những nhạc chuông đó có nhất thiết phải tìm các thư mục chứa nhạc chuông mặc định để lấy ra không ?
    - Không, có activity để có thể lấy nhạc chuông của thiết bị
    
    ```kotlin
    val pickSong =
            rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
                val toneUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    it.data?.getParcelableExtra(
                        RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java
                    )
                } else {
                    it.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
                }
                Log.d("", "Alarm: uri = $toneUri")
                setTone(toneUri ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
            }
    
    val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
    intent.putExtra(
        RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM
    )
    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Tone")
    intent.putExtra(
        RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
    )
    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
    
    pickSong.launch(intent)
    ```
    

- Các thiết bị hiện đại bây giờ thường chia nhiều loại âm thanh, phần lớn là 4 loại: Media, Ringtone, Notification, Alarm. Nếu dùng thư viện Media trong android thì sẽ phát ở loại media. Nhưng báo thức phải được phát ở loại Alarm thì làm thế nào ?
    - Chưa biết các để kêu ở loại Alarm nhưng nếu mình dùng thư viện Ringtone thay thế thì nhạc sẽ phát ở Ringtone. Tạm chấp nhận vì Media thường sẽ lẫn với âm thanh của các ứng dụng media và dễ bị người dùng tắt.
    

![z4610788670573_8037c2128b55a0d0b5ecd699672e5fe5.jpg](Alarm%20b797c4d0c7694924a529122dfa60fdf2/z4610788670573_8037c2128b55a0d0b5ecd699672e5fe5.jpg)