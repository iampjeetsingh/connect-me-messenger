package com.connectme.messenger.call;

import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallDirection;
import com.sinch.android.rtc.calling.CallEndCause;
import com.connectme.messenger.Auth;
import com.connectme.messenger.Database;
import com.connectme.messenger.model.CallLog;

public class CallManager {
    public static void generateCallLog(Call call){
        boolean incoming;
        if(call.getDirection()== CallDirection.INCOMING)
            incoming=true;
        else
            incoming=false;
        CallEndCause callEndCause = call.getDetails().getEndCause();
        CallLog callLog = new CallLog();
        callLog.setCallId(call.getCallId());
        callLog.setRemotedUserId(call.getRemoteUserId());
        callLog.setDuration(call.getDetails().getDuration());
        callLog.setEstablishedTime(call.getDetails().getEstablishedTime());
        callLog.setEndedTime(call.getDetails().getEndedTime());
        callLog.setVideoCall(call.getDetails().isVideoOffered());
        callLog.setIncoming(incoming);
        if(callEndCause==CallEndCause.CANCELED || callEndCause==CallEndCause.NO_ANSWER){
            callLog.setAnswered(false);
        }else if(callEndCause==CallEndCause.HUNG_UP || callEndCause==CallEndCause.DENIED){
            callLog.setAnswered(true);
        }else if(callEndCause==CallEndCause.TIMEOUT){
            callLog.setAnswered(false);
            CallLog remoteUserCallLog;
            remoteUserCallLog = callLog;
            remoteUserCallLog.setIncoming(!incoming);
            remoteUserCallLog.setRemotedUserId(Auth.getUid());
            Database.getCallsRef(remoteUserCallLog.getRemotedUserId()).push().setValue(remoteUserCallLog);
        }
        Database.getCallsRef().push().setValue(callLog);
    }
}
