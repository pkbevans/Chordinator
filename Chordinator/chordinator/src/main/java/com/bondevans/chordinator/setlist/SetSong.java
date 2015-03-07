package com.bondevans.chordinator.setlist;

import android.os.Parcel;
import android.os.Parcelable;

public class SetSong implements Parcelable{
	public static Creator CREATOR = new SetSongCreater();
	public long id;
	public String title;
	public String artist;
	public String composer;
	public String filePath;
	public int setOrder;

	public SetSong(long id, String title, String filePath, int setOrder){
		this.id = id;
		this.title = title;
		this.filePath = filePath;
		this.setOrder = setOrder;
	}
	public SetSong(long id, String title, String artist, String composer, String filePath, int setOrder){
		this.id = id;
		this.title = title;
		this.artist = artist;
		this.composer = composer;
		this.filePath = filePath;
		this.setOrder = setOrder;
	}

	public SetSong(Parcel parcel) {
		id = parcel.readLong();
		title = parcel.readString();
		artist = parcel.readString();
		composer = parcel.readString();
		filePath = parcel.readString();
		setOrder = parcel.readInt();
	}

	public String toString(){
		return title;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int i) {
		parcel.writeLong(id);
		parcel.writeString(title);
		parcel.writeString(artist);
		parcel.writeString(composer);
		parcel.writeString(filePath);
		parcel.writeInt(setOrder);
	}
	/**
	 * It will be required during un-marshaling data stored in a Parcel
	 */
	public static final class SetSongCreater implements Parcelable.Creator<SetSong> {
		public SetSong createFromParcel(Parcel source) {
			return new SetSong(source);
		}
		public SetSong[] newArray(int size) {
			return new SetSong[size];
		}
	}
}
