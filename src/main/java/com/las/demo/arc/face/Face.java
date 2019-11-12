package com.las.demo.arc.face;

import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.FaceInfo;
import lombok.Data;

@Data
public class Face {

    private String name;
    //人脸信息
    private FaceInfo info;
    //特征值
    private FaceFeature feature;

    public Face(String name, FaceInfo info, FaceFeature feature) {
        this.name = name;
        this.info = info;
        this.feature = feature;
    }
}
