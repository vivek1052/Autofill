package com.example.autofill.dataClass;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.autofill.AutofillId;

public class ParsedStructure implements Parcelable {
    public AutofillId nodeId;
    public String autofillhint;
    public String text;

    public ParsedStructure(AutofillId nodeId, String autofillhint, String text){
        this.nodeId = nodeId;
        this.autofillhint = autofillhint;
        this.text = text;
    }

    protected ParsedStructure(Parcel in) {
        nodeId = in.readParcelable(AutofillId.class.getClassLoader());
        autofillhint = in.readString();
        text = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(nodeId, flags);
        dest.writeString(autofillhint);
        dest.writeString(text);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ParsedStructure> CREATOR = new Creator<ParsedStructure>() {
        @Override
        public ParsedStructure createFromParcel(Parcel in) {
            return new ParsedStructure(in);
        }

        @Override
        public ParsedStructure[] newArray(int size) {
            return new ParsedStructure[size];
        }
    };

    public AutofillId getNodeId() {
        return nodeId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setNodeId(AutofillId nodeId) {
        this.nodeId = nodeId;
    }

    public String getAutofillhint() {
        return autofillhint;
    }

    public void setAutofillhint(String autofillhint) {
        this.autofillhint = autofillhint;
    }
}
