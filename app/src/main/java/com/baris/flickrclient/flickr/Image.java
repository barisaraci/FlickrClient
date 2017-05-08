package com.baris.flickrclient.flickr;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by baris on 20/04/2017.
 */

public class Image implements Parcelable {

    private String id, farmId, serverId, secret, title, thumbnailURL, imageURL;
    private int no;

    public Image() {}

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(id);
        parcel.writeString(farmId);
        parcel.writeString(serverId);
        parcel.writeString(secret);
        parcel.writeString(title);
        parcel.writeString(thumbnailURL);
        parcel.writeString(imageURL);
        parcel.writeInt(no);
    }

    public static final Parcelable.Creator<Image> CREATOR = new Parcelable.Creator<Image>() {
        @Override
        public Image createFromParcel(Parcel source) {
            return new Image(source);
        }

        @Override
        public Image[] newArray(int size) {
            return new Image[size];
        }
    };

    private Image(Parcel in){
        id = in.readString();
        farmId = in.readString();
        serverId = in.readString();
        secret = in.readString();
        title = in.readString();
        thumbnailURL = in.readString();
        imageURL = in.readString();
        no = in.readInt();
    }

    public void createLinks() {
        thumbnailURL = "https://farm" + farmId + ".staticflickr.com/" + serverId + "/" + id + "_" + secret + "_q.jpg";
        imageURL = "https://farm" + farmId + ".staticflickr.com/" + serverId + "/" + id + "_" + secret + "_b.jpg";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFarmId() {
        return farmId;
    }

    public void setFarmId(String farmId) {
        this.farmId = farmId;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getThumbnailURL() {
        return thumbnailURL;
    }

    public void setThumbnailURL(String thumbnailURL) {
        this.thumbnailURL = thumbnailURL;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public int getNo() {
        return no;
    }

    public void setNo(int no) {
        this.no = no;
    }

}
