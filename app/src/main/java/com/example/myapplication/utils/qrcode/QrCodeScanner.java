package com.example.myapplication.utils.qrcode;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class QrCodeScanner {
    private static final String TAG = "QrCodeScanner";

    public interface ScanResultCallback {
        void onSuccess(String result);
        void onError(String errorMessage);
    }

    private static final int REQUEST_CAMERA = 1001;
    private static final int REQUEST_GALLERY = 1002;
    public static final int REQUEST_PERMISSION = 1003;

    private static ScanResultCallback callback;
    private static Activity currentActivity;

    /**
     * 启动扫码流程
     * @param activity 当前Activity
     * @param cb 扫码结果回调
     */
    public static void startScan(Activity activity, ScanResultCallback cb) {
        Log.d(TAG, "startScan: 开始扫码流程");
        currentActivity = activity;
        callback = cb;

        if (checkPermissions_1()) {
            Log.d(TAG, "startScan: 权限已授予，显示扫码选项");
            showScanOptions(); // 已有权限直接显示选项
        } else {
            Log.d(TAG, "startScan: 权限未授予，请求权限");
            requestPermissions(); // 无权限时请求
        }
    }

    /**
     * 检查所需权限是否已授予
     * @return true表示所有权限已授予，false表示至少有一个权限未授予
     */
    private static boolean checkPermissions_1() {
        Log.d(TAG, "checkPermissions_1: 开始检查权限");
        String[] requiredPerms = getRequiredPermissions();
        Log.d(TAG, "checkPermissions_1: 需要检查的权限数量: " + requiredPerms.length);

        for (String perm : requiredPerms) {
            Log.d(TAG, "checkPermissions_1: 检查权限: " + perm);
            int permissionCheck = ContextCompat.checkSelfPermission(currentActivity, perm);
            Log.d(TAG, "checkPermissions_1: 权限检查结果: " + permissionCheck + " (PERMISSION_GRANTED=" + PackageManager.PERMISSION_GRANTED + ")");

            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "checkPermissions_1: 权限未授予: " + perm);
                return false;
            } else {
                Log.d(TAG, "checkPermissions_1: 权限已授予: " + perm);
            }
        }
        Log.d(TAG, "checkPermissions_1: 所有权限均已授予");
        return true;
    }

    /**
     * 检查摄像头是否可用
     * @param context 上下文
     * @return true表示有可用的后置摄像头，false表示没有
     */
    public static boolean isCameraAvailable(Context context) {
        Log.d(TAG, "isCameraAvailable: 检查摄像头是否可用");
        try {
            CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_BACK) {
                    Log.d(TAG, "isCameraAvailable: 检测到后置摄像头");
                    return true;
                }
            }
            Log.d(TAG, "isCameraAvailable: 未检测到后置摄像头");
        } catch (Exception e) {
            Log.e(TAG, "isCameraAvailable: 摄像头检测失败", e);
        }
        return false;
    }

    /**
     * 请求所需权限
     */
    private static void requestPermissions() {
        Log.d(TAG, "requestPermissions: 请求权限");
        String[] requiredPerms = getRequiredPermissions();
        Log.d(TAG, "requestPermissions: 请求权限数量: " + requiredPerms.length);
        for (String perm : requiredPerms) {
            Log.d(TAG, "requestPermissions: 请求权限: " + perm);
        }

        ActivityCompat.requestPermissions(
                currentActivity,
                requiredPerms,
                REQUEST_PERMISSION
        );
        Log.d(TAG, "requestPermissions: 权限请求已发送");
    }

    /**
     * 获取所需权限列表
     * @return 权限数组
     */
    private static String[] getRequiredPermissions() {
        Log.d(TAG, "getRequiredPermissions: 获取所需权限列表");
        List<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.CAMERA);
        Log.d(TAG, "getRequiredPermissions: 添加 CAMERA 权限");

        // 注意：这里的权限检查被注释掉了，只添加了CAMERA权限
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.TIRAMISU) {
            // permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            Log.d(TAG, "getRequiredPermissions: Android版本 <= TIRAMISU，但未添加存储权限");
        } else {
            // permissions.add(Manifest.permission.READ_MEDIA_IMAGES);
            Log.d(TAG, "getRequiredPermissions: Android版本 > TIRAMISU，但未添加存储权限");
        }

        String[] result = permissions.toArray(new String[0]);
        Log.d(TAG, "getRequiredPermissions: 最终权限列表大小: " + result.length);
        for (String perm : result) {
            Log.d(TAG, "getRequiredPermissions: 权限项: " + perm);
        }
        return result;
    }

    /**
     * 处理权限请求结果
     * @param requestCode 请求码
     * @param grantResults 授权结果
     */
    public static void handlePermissionResult(int requestCode, int[] grantResults) {
        Log.d(TAG, "handlePermissionResult: 处理权限结果, requestCode=" + requestCode + ", grantResults长度=" + grantResults.length);

        if (requestCode == REQUEST_PERMISSION) {
            boolean allGranted = true;
            String[] requestedPermissions = getRequiredPermissions();

            Log.d(TAG, "handlePermissionResult: 处理扫码权限请求结果");
            Log.d(TAG, "handlePermissionResult: 请求的权限数量: " + requestedPermissions.length);
            Log.d(TAG, "handlePermissionResult: 授权结果数量: " + grantResults.length);

            // 检查权限数量是否匹配
            if (requestedPermissions.length != grantResults.length) {
                Log.w(TAG, "handlePermissionResult: 权限数量不匹配，请求: " + requestedPermissions.length + ", 结果: " + grantResults.length);
            }

            for (int i = 0; i < Math.min(grantResults.length, requestedPermissions.length); i++) {
                String perm = requestedPermissions[i];
                int result = grantResults[i];

                Log.d(TAG, "handlePermissionResult: 权限 " + perm + " 授权结果: " + result);

                if (result != PackageManager.PERMISSION_GRANTED) {
                    Log.w(TAG, "handlePermissionResult: 权限被拒绝: " + perm);
                    allGranted = false;
                } else {
                    Log.d(TAG, "handlePermissionResult: 权限已授予: " + perm);
                }
            }

            if (allGranted) {
                Log.d(TAG, "handlePermissionResult: 所有权限均已授予，显示扫码选项");
                showScanOptions();
            } else {
                Log.w(TAG, "handlePermissionResult: 部分权限被拒绝");
                if (callback != null) {
                    callback.onError("需要相机权限才能进行扫码");
                }
            }
        } else {
            Log.d(TAG, "handlePermissionResult: 非扫码权限请求，requestCode=" + requestCode);
        }
    }

    /**
     * 处理Activity返回结果
     * @param requestCode 请求码
     * @param resultCode 结果码
     * @param data 返回数据
     */
    public static void handleActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "handleActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (requestCode == REQUEST_CAMERA) {
            Log.d(TAG, "handleActivityResult: 处理摄像头扫码结果");
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (result != null && result.getContents() != null) {
                Log.d(TAG, "handleActivityResult: 扫码成功，内容: " + result.getContents());
                if (callback != null) {
                    callback.onSuccess(result.getContents());
                }
            } else {
                Log.d(TAG, "handleActivityResult: 扫码取消或失败");
                if (callback != null) {
                    callback.onError("扫码取消或失败");
                }
            }
        } else if (requestCode == REQUEST_GALLERY && resultCode == Activity.RESULT_OK && data != null) {
            Log.d(TAG, "handleActivityResult: 处理相册选择结果");
            decodeQrFromGallery(data.getData());
        } else {
            Log.d(TAG, "handleActivityResult: 其他请求或结果");
        }
    }

    /**
     * 显示扫码选项对话框
     */
    public static void showScanOptions() {
        Log.d(TAG, "showScanOptions: 显示扫码选项对话框");
        if (currentActivity == null) {
            Log.e(TAG, "showScanOptions: currentActivity 为 null");
            return;
        }

        try {
            new AlertDialog.Builder(currentActivity)
                    .setTitle("选择扫码方式")
                    .setItems(new String[]{"摄像头扫码", "从相册选择"}, (dialog, which) -> {
                        Log.d(TAG, "showScanOptions: 用户选择选项: " + which);
                        switch (which) {
                            case 0:
                                Log.d(TAG, "showScanOptions: 选择摄像头扫码");
                                startCameraScan();
                                break;
                            case 1:
                                Log.d(TAG, "showScanOptions: 选择从相册选择");
                                pickImageFromGallery();
                                break;
                            default:
                                Log.w(TAG, "showScanOptions: 未知选项: " + which);
                        }
                    })
                    .setNegativeButton("取消", (dialog, which) -> {
                        Log.d(TAG, "showScanOptions: 用户取消操作");
                    })
                    .show();
            Log.d(TAG, "showScanOptions: 对话框显示成功");
        } catch (Exception e) {
            Log.e(TAG, "showScanOptions: 显示对话框失败", e);
            if (callback != null) {
                callback.onError("显示扫码选项失败: " + e.getMessage());
            }
        }
    }

    /**
     * 启动摄像头扫码
     */
    // 摄像头扫码
private static void startCameraScan() {
    Log.d(TAG, "startCameraScan: 启动摄像头扫码");
    if (currentActivity == null) {
        Log.e(TAG, "startCameraScan: currentActivity 为 null");
        if (callback != null) {
            callback.onError("Activity不可用");
        }
        return;
    }

    try {
        IntentIntegrator integrator = new IntentIntegrator(currentActivity);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("将二维码放入框内，即可自动扫描");
        integrator.setCameraId(0);  // 0表示后置摄像头
        integrator.setBeepEnabled(true);  // 扫描成功后播放提示音
        integrator.setBarcodeImageEnabled(true);  // 显示扫描到的二维码图像
        integrator.setOrientationLocked(false);  // 不锁定屏幕方向

        // 使用自定义的扫描Activity
        integrator.setCaptureActivity(CustomScannerActivity.class);

        integrator.initiateScan();
        Log.d(TAG, "startCameraScan: 摄像头扫码已启动");
    } catch (Exception e) {
        Log.e(TAG, "startCameraScan: 启动摄像头扫码失败", e);
        if (callback != null) {
            callback.onError("启动摄像头失败: " + e.getMessage());
        }
    }
}


    /**
     * 从相册选择图片
     */
    private static void pickImageFromGallery() {
        Log.d(TAG, "pickImageFromGallery: 从相册选择图片");
        if (currentActivity == null) {
            Log.e(TAG, "pickImageFromGallery: currentActivity 为 null");
            if (callback != null) {
                callback.onError("Activity不可用");
            }
            return;
        }

        try {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            currentActivity.startActivityForResult(intent, REQUEST_GALLERY);
            Log.d(TAG, "pickImageFromGallery: 已启动相册选择");
        } catch (Exception e) {
            Log.e(TAG, "pickImageFromGallery: 启动相册选择失败", e);
            if (callback != null) {
                callback.onError("打开相册失败: " + e.getMessage());
            }
        }
    }

    /**
     * 从相册图片中解码二维码
     * @param imageUri 图片URI
     */
    private static void decodeQrFromGallery(Uri imageUri) {
        Log.d(TAG, "decodeQrFromGallery: 从相册图片解码二维码");
        if (currentActivity == null) {
            Log.e(TAG, "decodeQrFromGallery: currentActivity 为 null");
            if (callback != null) {
                callback.onError("Activity不可用");
            }
            return;
        }

        try {
            InputStream inputStream = currentActivity.getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            Log.d(TAG, "decodeQrFromGallery: 图片加载成功，尺寸: " + bitmap.getWidth() + "x" + bitmap.getHeight());

            String result = decodeQrFromBitmap(bitmap);
            if (result != null && callback != null) {
                Log.d(TAG, "decodeQrFromGallery: 二维码识别成功: " + result);
                callback.onSuccess(result);
            } else if (callback != null) {
                Log.d(TAG, "decodeQrFromGallery: 未识别到二维码");
                callback.onError("未识别到二维码");
            }
        } catch (Exception e) {
            Log.e(TAG, "decodeQrFromGallery: 图片处理失败", e);
            if (callback != null) {
                callback.onError("图片处理失败: " + e.getMessage());
            }
        }
    }

    /**
     * 从Bitmap中解码二维码
     * @param bitmap 图片Bitmap
     * @return 二维码内容，如果未识别到则返回null
     */
    private static String decodeQrFromBitmap(Bitmap bitmap) {
        Log.d(TAG, "decodeQrFromBitmap: 从Bitmap解码二维码");
        try {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int[] pixels = new int[width * height];
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

            RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);
            BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));

            String result = new MultiFormatReader().decode(binaryBitmap).getText();
            Log.d(TAG, "decodeQrFromBitmap: 二维码解码成功: " + result);
            return result;
        } catch (Exception e) {
            Log.w(TAG, "decodeQrFromBitmap: 二维码解码失败", e);
            return null;
        }
    }
}
