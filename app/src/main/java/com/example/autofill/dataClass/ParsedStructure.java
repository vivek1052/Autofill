package com.example.autofill.dataClass;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.autofill.AutofillId;

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(nodeId, i);
        parcel.writeString(autofillhint);
    }

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
