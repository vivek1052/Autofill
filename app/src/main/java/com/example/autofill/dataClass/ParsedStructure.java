package com.example.autofill.dataClass;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.autofill.AutofillId;

import java.util.ArrayList;

public class ParsedStructure implements Parcelable {
    public AutofillId nodeId;
    public String autofillhint;

    public ParsedStructure(AutofillId nodeId, String autofillhint){
        this.nodeId = nodeId;
        this.autofillhint = autofillhint;
    }

    protected ParsedStructure(Parcel in) {
        nodeId = in.readParcelable(AutofillId.class.getClassLoader());
        autofillhint = in.readString();
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(nodeId, flags);
        dest.writeString(autofillhint);
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
