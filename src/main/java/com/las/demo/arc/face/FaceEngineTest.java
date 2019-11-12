package com.las.demo.arc.face;


import com.arcsoft.face.*;
import com.arcsoft.face.enums.ErrorInfo;
import com.arcsoft.face.enums.ImageFormat;
import com.arcsoft.face.toolkit.ImageFactory;
import com.arcsoft.face.toolkit.ImageInfo;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Slf4j
public class FaceEngineTest {

    public static void main(String[] args) throws URISyntaxException {
        String pathFaceDb = System.getProperty("dir.face.db", "./image/db");
        String pathCheck = System.getProperty("dir.face.check", "./image/check");

        File dirImageDB = new File(pathFaceDb);
        if (!dirImageDB.isDirectory()) {
            log.error("pathFaceDb {} is not dir", pathFaceDb);
            return;
        }

        File dirImageCheck = new File(pathCheck);
        if (!dirImageDB.isDirectory()) {
            log.error("pathCheck {} is not dir", pathCheck);
            return;
        }

        FaceEngine faceEngine = FaceEngines.instance();

        List<Face> facesDB = Lists.newLinkedList();

        log.info("##################### load face to memory ###################");

        Arrays.stream(dirImageDB.listFiles()).forEach(file -> {
            List<Face> faces = file2Faces(file);
            facesDB.addAll(faces);
        });

        log.info("##################### load check ###################");
        List<Face> checkFaces = Lists.newLinkedList();
        Arrays.stream(dirImageCheck.listFiles()).forEach(file -> {
            List<Face> faces = file2Faces(file);
            checkFaces.addAll(faces);
        });

        log.info("##################### check now ###################");
        checkFaces.forEach(source -> {
            facesDB.forEach(target -> {
                //特征比对
                FaceFeature targetFaceFeature = target.getFeature();
                FaceFeature sourceFaceFeature = source.getFeature();
                FaceSimilar faceSimilar = new FaceSimilar();
                int compareCode = faceEngine.compareFaceFeature(targetFaceFeature, sourceFaceFeature, faceSimilar);
                log.info("source {}, target {}, face compare score {}", source.getName(), target.getName(), faceSimilar.getScore());
            });
        });

        FaceEngines.destory();
    }

    public static List<Face> file2Faces(File file) {
        FaceEngine faceEngine = FaceEngines.instance();
        //人脸检测
        ImageInfo imageInfo = ImageFactory.getRGBData(file);
        List<FaceInfo> faceInfoList = new ArrayList<FaceInfo>();
        int detectCode = faceEngine.detectFaces(imageInfo.getImageData(), imageInfo.getWidth(), imageInfo.getHeight(), ImageFormat.CP_PAF_BGR24, faceInfoList);
        log.info("{}, faceInfo size: {}", file.getName(), faceInfoList.size());

        List<Face> faces = faceInfoList.stream().map(faceInfo -> {
            //特征提取
            FaceFeature feature = new FaceFeature();
            int extractCode2 = faceEngine.extractFaceFeature(imageInfo.getImageData(), imageInfo.getWidth(), imageInfo.getHeight(), ImageFormat.CP_PAF_BGR24, faceInfo, feature);
            log.info("{}, faceInfo: {}, feature md5: {}", file.getName(), faceInfo, MD5(feature.getFeatureData()));
            Face face = new Face(file.getName(), faceInfo, feature);
            return face;
        }).collect(Collectors.toList());
        return faces;
    }

    public static void processIrFaceInfo(String pathImage3) {
        FaceEngine faceEngine = FaceEngines.instance();
        //IR属性处理
        ImageInfo imageInfoGray = ImageFactory.getGrayData(new File(pathImage3));
        List<FaceInfo> faceInfoListGray = new ArrayList<FaceInfo>();
        int detectCodeGray = faceEngine.detectFaces(imageInfoGray.getImageData(), imageInfoGray.getWidth(), imageInfoGray.getHeight(), ImageFormat.CP_PAF_GRAY, faceInfoListGray);

        FunctionConfiguration configuration2 = new FunctionConfiguration();
        configuration2.setSupportIRLiveness(true);
        int processCode2 = faceEngine.processIr(imageInfoGray.getImageData(), imageInfoGray.getWidth(), imageInfoGray.getHeight(), ImageFormat.CP_PAF_GRAY, faceInfoListGray, configuration2);
        //IR活体检测
        List<IrLivenessInfo> irLivenessInfo = new ArrayList<>();
        int livenessIr = faceEngine.getLivenessIr(irLivenessInfo);
        System.out.println("IR活体：" + irLivenessInfo.get(0).getLiveness());

        //设置活体检测参数
        int paramCode = faceEngine.setLivenessParam(0.8f, 0.8f);

        //获取激活文件信息
        ActiveFileInfo activeFileInfo = new ActiveFileInfo();
        int activeFileCode = faceEngine.getActiveFileInfo(activeFileInfo);
    }

    //人脸属性检测
    public static void processFaceInfo(ImageInfo imageInfo, List<FaceInfo> faceInfoList) {
        FaceEngine faceEngine = FaceEngines.instance();

        //人脸属性检测
        FunctionConfiguration configuration = new FunctionConfiguration();
        configuration.setSupportAge(true);
        configuration.setSupportFace3dAngle(true);
        configuration.setSupportGender(true);
        configuration.setSupportLiveness(true);
        int processCode = faceEngine.process(imageInfo.getImageData(), imageInfo.getWidth(), imageInfo.getHeight(), ImageFormat.CP_PAF_BGR24, faceInfoList, configuration);

        //性别检测
        List<GenderInfo> genderInfoList = new ArrayList<GenderInfo>();
        int genderCode = faceEngine.getGender(genderInfoList);
        //assertEquals("性别检测失败", genderCode, ErrorInfo.MOK.getValue());
        assert Objects.equals(genderCode, ErrorInfo.MOK.getValue());
        System.out.println("性别：" + genderInfoList.get(0).getGender());

        //年龄检测
        List<AgeInfo> ageInfoList = new ArrayList<AgeInfo>();
        int ageCode = faceEngine.getAge(ageInfoList);
        //assertEquals("年龄检测失败", ageCode, ErrorInfo.MOK.getValue());
        assert Objects.equals(ageCode, ErrorInfo.MOK.getValue());
        System.out.println("年龄：" + ageInfoList.get(0).getAge());

        //3D信息检测
        List<Face3DAngle> face3DAngleList = new ArrayList<Face3DAngle>();
        int face3dCode = faceEngine.getFace3DAngle(face3DAngleList);
        System.out.println("3D角度：" + face3DAngleList.get(0).getPitch() + "," + face3DAngleList.get(0).getRoll() + "," + face3DAngleList.get(0).getYaw());

        //活体检测
        List<LivenessInfo> livenessInfoList = new ArrayList<LivenessInfo>();
        int livenessCode = faceEngine.getLiveness(livenessInfoList);
        System.out.println("活体：" + livenessInfoList.get(0).getLiveness());
    }

    public static String MD5(byte[] input) {
        try {
            // 生成一个MD5加密计算摘要
            MessageDigest md = MessageDigest.getInstance("MD5");
            // 计算md5函数
            md.update(input);
            // digest()最后确定返回md5 hash值，返回值为8为字符串。因为md5 hash值是16位的hex值，实际上就是8位的字符
            // BigInteger函数则将8位的字符串转换成16位hex值，用字符串来表示；得到字符串形式的hash值
            return new BigInteger(1, md.digest()).toString(16);
        } catch (Exception e) {
            log.error("MD5加密出现错误" + e.toString());
        }
        return "";
    }

}
