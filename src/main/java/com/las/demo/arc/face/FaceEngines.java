package com.las.demo.arc.face;

import com.arcsoft.face.EngineConfiguration;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FunctionConfiguration;
import com.arcsoft.face.enums.DetectMode;
import com.arcsoft.face.enums.DetectOrient;
import com.arcsoft.face.enums.ErrorInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
public class FaceEngines {
    private static String SO_LIB_PATH;
    private static String APP_ID;
    private static String SDK_KEY;

    static {
        SO_LIB_PATH = System.getProperty("jni.library.path");
        log.info("jni.library.path: {}", SO_LIB_PATH);
        APP_ID = System.getProperty("arc.face.appId", "GfGcTkGreZ8NBRAxZM8n5nyQt71ZAUx9ik2pHbgi6ZLS");
        log.info("APP_ID: {}", APP_ID);
        SDK_KEY = System.getProperty("arc.face.sdkKey", "8BMDRTdfPPck2qcL6vGrEW8QM2kxw7vxwy5Awn6ngTEk");
        log.info("SDK_KEY: {}", SDK_KEY);
    }

    private volatile static FaceEngine faceEngine;

    public static FaceEngine instance() {
        if (Objects.isNull(faceEngine)) {
            synchronized (faceEngine) {
                if (Objects.isNull(faceEngine)) {
                    faceEngine = new FaceEngine(SO_LIB_PATH);
                    activeOnline();
                }
            }
        }
        return faceEngine;
    }

    public static void activeOnline() {
        int activeCode = instance().activeOnline(APP_ID, SDK_KEY);
        if (activeCode != ErrorInfo.MOK.getValue() && activeCode != ErrorInfo.MERR_ASF_ALREADY_ACTIVATED.getValue()) {
            log.error("引擎激活失败");
        }
    }

    public static void init(){
        //引擎配置
        EngineConfiguration engineConfiguration = new EngineConfiguration();
        engineConfiguration.setDetectMode(DetectMode.ASF_DETECT_MODE_IMAGE);
        engineConfiguration.setDetectFaceOrientPriority(DetectOrient.ASF_OP_0_ONLY);

        //功能配置
        FunctionConfiguration functionConfiguration = new FunctionConfiguration();
        functionConfiguration.setSupportAge(true);
        functionConfiguration.setSupportFace3dAngle(true);
        functionConfiguration.setSupportFaceDetect(true);
        functionConfiguration.setSupportFaceRecognition(true);
        functionConfiguration.setSupportGender(true);
        functionConfiguration.setSupportLiveness(true);
        functionConfiguration.setSupportIRLiveness(true);
        engineConfiguration.setFunctionConfiguration(functionConfiguration);

        //初始化引擎
        int initCode = faceEngine.init(engineConfiguration);

        if (initCode != ErrorInfo.MOK.getValue()) {
            System.out.println("初始化引擎失败");
        }
    }

    //引擎卸载
    public static void destory() {
        int unInitCode = faceEngine.unInit();
    }
}
