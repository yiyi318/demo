package com.example.myapplication.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class Chapter implements Parcelable{
    private List<Chapter> children;
    private int courseId;
    private int id;
    private String name;
    private int order;
    private int parentChapterId;
    private int visible;

    // 空构造方法
    public Chapter() {
    }

    // Parcelable 构造方法
    protected Chapter(Parcel in) {
        children = in.createTypedArrayList(Chapter.CREATOR);
        courseId = in.readInt();
        id = in.readInt();
        name = in.readString();
        order = in.readInt();
        parentChapterId = in.readInt();
        visible = in.readInt();
    }

    // Parcelable  Creator
    public static final Parcelable.Creator<Chapter> CREATOR = new Parcelable.Creator<Chapter>() {
        @Override
        public Chapter createFromParcel(Parcel in) {
            return new Chapter(in);
        }

        @Override
        public Chapter[] newArray(int size) {
            return new Chapter[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(children);
        dest.writeInt(courseId);
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeInt(order);
        dest.writeInt(parentChapterId);
        dest.writeInt(visible);
    }

    // Getter 方法
    public List<Chapter> getChildren() {
        return children;
    }

    public int getCourseId() {
        return courseId;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getOrder() {
        return order;
    }

    public int getParentChapterId() {
        return parentChapterId;
    }

    public int getVisible() {
        return visible;
    }

    // Setter 方法
    public void setChildren(List<Chapter> children) {
        this.children = children;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public void setParentChapterId(int parentChapterId) {
        this.parentChapterId = parentChapterId;
    }

    public void setVisible(int visible) {
        this.visible = visible;
    }
}
