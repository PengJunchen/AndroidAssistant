package com.pjc.accessibilityservice;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import java.util.List;

public class AutoBuyService extends AccessibilityService {


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.d(Constants.LOG_TAG, String.format("PackageName is %s, ClassName is %s",event.getPackageName(), event.getClassName()));

        String packageName = event.getPackageName().toString();
        if (packageName == null){
            return;
        }

        switch (packageName){
            case "com.yaya.zone":
                handleDingDong(event);
                break;
            case "com.wudaokou.hippo":
                handleHippo(event);
                break;
            case "cn.missfresh.application":
                handleMiFresh(event);
                break;
            default:
                // todo support other apps later
                break;
        }

    }

    private boolean findNodeListByTextAndClick(AccessibilityEvent event, String text){
        List<AccessibilityNodeInfo> accessibilityNodeInfoList = findNodeListByText(event, text);
        if (accessibilityNodeInfoList != null && !accessibilityNodeInfoList.isEmpty()) {
            accessibilityNodeInfoList.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
            return true;
        }
        return false;
    }

    private boolean findNodeListByTextParentClick(AccessibilityEvent event, String text){
        List<AccessibilityNodeInfo> accessibilityNodeInfoList = findNodeListByText(event, text);
        if (accessibilityNodeInfoList != null && !accessibilityNodeInfoList.isEmpty()) {
            accessibilityNodeInfoList.get(0).getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
            return true;
        }
        return false;
    }

    private boolean findNodeListByTextParentTwiceClick(AccessibilityEvent event, String text){
        List<AccessibilityNodeInfo> accessibilityNodeInfoList = findNodeListByText(event, text);
        if (accessibilityNodeInfoList != null && !accessibilityNodeInfoList.isEmpty()) {
            accessibilityNodeInfoList.get(0).getParent().getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
            return true;
        }
        return false;
    }

    private List<AccessibilityNodeInfo> findNodeListByText(AccessibilityEvent event, String text){
        List<AccessibilityNodeInfo> accessibilityNodeInfoList = event.getSource().findAccessibilityNodeInfosByText(text);
        if (accessibilityNodeInfoList != null && !accessibilityNodeInfoList.isEmpty()) {
            return accessibilityNodeInfoList;
        }
        return null;
    }

    private List<AccessibilityNodeInfo> findNodeListByViewId(AccessibilityEvent event, String viewId){
        List<AccessibilityNodeInfo> accessibilityNodeInfoList = event.getSource().findAccessibilityNodeInfosByText(viewId);
        if (accessibilityNodeInfoList != null && !accessibilityNodeInfoList.isEmpty()) {
            return accessibilityNodeInfoList;
        }
        return null;
    }

    private void deplay(){
        try{
            Thread.sleep(300);
        }catch (Exception e){

        }
    }

    @Override
    public void onInterrupt() {

    }
    // Common --------------------------------------------------------------------------------------
    // ????????????????????????-------------------------------------------------------------------------------------
    private void dingdongPayingEffective(AccessibilityEvent event){
        // ??????????????????????????????

        // com.yaya.zone:id/tv_no_coupon_sum_money ????????????
        try {
            List<AccessibilityNodeInfo> accessibilityNodeInfoList = findNodeListByText(event, "????????????");
            if (accessibilityNodeInfoList != null && !accessibilityNodeInfoList.isEmpty()) {
                AccessibilityNodeInfo parentNode = accessibilityNodeInfoList.get(0).getParent();
                int count = parentNode.getChildCount();
                while (count > 0) {
                    if (parentNode.getChild(count -1).getText() != null && parentNode.getChild(count - 1).getText().length() > 0) {
                        if (findNodeListByTextAndClick(event, DING_DONG_PAY_TEXT)) {
                            DING_DONG_STATUS = 3;  //????????????????????????????????????
                            break;
                        }
                    }
                    count--;
                }
                if (count <= 0) {
                    DING_DONG_STATUS = 2;
                    performGlobalAction(GLOBAL_ACTION_BACK);
                }
            } else {
                DING_DONG_STATUS = 1;
            }
        }catch (Exception ex){
            DING_DONG_STATUS = -1;
            handleDingDong(event);
        }

    }

    private void dingdongTimeEffective(AccessibilityEvent event){
        List<AccessibilityNodeInfo> accessibilityNodeInfoList = findNodeListByText(event, "-");
        if (accessibilityNodeInfoList != null && !accessibilityNodeInfoList.isEmpty()){
            int listCount = accessibilityNodeInfoList.size();
            while (listCount > 0){
                AccessibilityNodeInfo current = accessibilityNodeInfoList.get(listCount-1);
                AccessibilityNodeInfo parent = current.getParent();
                listCount--;
            }
        }
    }

    private String DING_DONG_SHOPPINGCART_TEXT = "?????????";
    private String DING_DONG_CHECKIN_TEXT = "?????????";
    private String DING_DONG_PAY_TEXT = "????????????";

    private int DING_DONG_STATUS = -1;

    private void handleDingDong(AccessibilityEvent event){

        if (event == null || event.getSource() == null){
            return;
        }
        // ??????????????????????????????
        if (findNodeListByText(event, DING_DONG_CHECKIN_TEXT) != null){
            DING_DONG_STATUS = 1;
        }
        if (findNodeListByText(event, DING_DONG_PAY_TEXT) != null){
            DING_DONG_STATUS = 2;
        }

        switch (DING_DONG_STATUS){
            case -1:{
                // ???????????????????????????????????????????????????????????????????????????????????????????????????
                if (findNodeListByText(event, DING_DONG_SHOPPINGCART_TEXT) != null){
                    DING_DONG_STATUS = 0;
                }
                if (findNodeListByText(event, DING_DONG_CHECKIN_TEXT) != null){
                    DING_DONG_STATUS = 1;
                }

            }
            break;
            case 0: {
                if (event.getClassName() == null) {
                    if (findNodeListByTextParentTwiceClick(event, DING_DONG_SHOPPINGCART_TEXT)) {
                        DING_DONG_STATUS = 1; //???????????????????????????
                    }
                }
                else {
                    String className = event.getClassName().toString();
                    if (className.equalsIgnoreCase("com.yaya.zone.activity.HomeActivity")) {
                        // Goto shopping cart
                        if (findNodeListByTextParentTwiceClick(event, DING_DONG_SHOPPINGCART_TEXT)) {
                            DING_DONG_STATUS = 1; //???????????????????????????
                        }
                    }
                }
            }
            break;
            case 1: {
                if (findNodeListByTextAndClick(event, DING_DONG_CHECKIN_TEXT)){
                    DING_DONG_STATUS = 2; //??????????????????????????????
                }
            }
            break;
            case 2: {
                // ?????????????????????
                dingdongPayingEffective(event);
            }
            break;
            case 3: {
                // ?????????????????????
                // ????????????????????????????????? ????????????????????????
                DING_DONG_STATUS = 2;
                Toast.makeText(getApplicationContext(), "Pay the money!", Toast.LENGTH_LONG).show();
            }
            break;
            default:
            break;
        }

        deplay();
    }

    // ???????????????????????? --------------------------------------------------------------------------------------
    private String HIPPO_SHOPPINGCART_TEXT = "?????????";
    private String HIPPO_CHECKIN_TEXT = "??????";
    private String HIPPO_PAY_TEXT = "????????????";


    private int HIPPO_STATUS = -1;

    private void handleHippo(AccessibilityEvent event){

        if (event == null || event.getSource() == null){
            return;
        }
        // ??????????????????????????????
        if (findNodeListByText(event, HIPPO_CHECKIN_TEXT) != null){
            HIPPO_STATUS = 1;
        }

        switch (HIPPO_STATUS){
            case -1:{
                // ???????????????????????????????????????????????????????????????????????????????????????????????????
                if (findNodeListByText(event, HIPPO_SHOPPINGCART_TEXT) != null){
                    HIPPO_STATUS = 0;
                }
                if (findNodeListByText(event, HIPPO_CHECKIN_TEXT) != null){
                    HIPPO_STATUS = 1;
                }

            }
            break;
            case 0: {
                // Goto shopping cart
                if (findNodeListByTextAndClick(event, HIPPO_SHOPPINGCART_TEXT)) {
                    HIPPO_STATUS = 1; //???????????????????????????
                }
            }
            break;
            case 1: {
                if (findNodeListByTextAndClick(event, HIPPO_CHECKIN_TEXT)){
                    HIPPO_STATUS = 2; //??????????????????????????????
                }
            }
            break;
            case 2: {
                // ?????????????????????
                if (findNodeListByTextAndClick(event, HIPPO_PAY_TEXT)){
                    HIPPO_STATUS = 3;
                }else{
                    performGlobalAction(GLOBAL_ACTION_BACK);
                    HIPPO_STATUS = 1;
                }
            }
            break;
            case 3: {
                HIPPO_STATUS = 2;
                Toast.makeText(getApplicationContext(), "Pay the money!", Toast.LENGTH_LONG).show();
            }
            break;
            default:
                break;
        }

        deplay();
    }

    // ?????????????????????????????? Not Complete as should search the whole ui tree to check content description --------------------------------------------------------------------------------------
    private String MI_FRESH_SHOPPINGCART_TEXT = "?????????";
    private String MI_FRESH_PAY_TEXT = "????????????";
    private String MI_FRESH_FAILURE = "?????????";

    private int MI_FRESH_STATUS = -1;
    private void handleMiFresh(AccessibilityEvent event){
        if (event == null || event.getSource() == null){
            return;
        }

        switch (MI_FRESH_STATUS){
            case -1:{
                if (findNodeListByText(event, MI_FRESH_SHOPPINGCART_TEXT) != null){
                    MI_FRESH_STATUS = 0;
                }
            }
            break;
            case 0:{
                if (event.getClassName() == null) {
                    // Try Goto shopping cart
                    if (findNodeListByTextParentClick(event, MI_FRESH_SHOPPINGCART_TEXT)) {
                        MI_FRESH_STATUS = 1; //???????????????????????????
                    }
                }else {
                    String className = event.getClassName().toString();
                    if (className.equalsIgnoreCase("cn.missfresh.module.main.view.MainActivity")) {
                        // Goto shopping cart
                        if (findNodeListByTextParentClick(event, MI_FRESH_SHOPPINGCART_TEXT)) {
                            MI_FRESH_STATUS = 1; //???????????????????????????
                        }
                    }
                }
            }
            break;
            case 1:{
                //
                try{
                    if (event.getSource().findAccessibilityNodeInfosByText(MI_FRESH_SHOPPINGCART_TEXT).get(0).getParent().getParent().getParent()
                            .getChild(0).getChild(0).getChild(0).getChild(0).getChild(0).getChild(0).getChild(10).performAction(AccessibilityNodeInfo.ACTION_CLICK)){
                        MI_FRESH_STATUS = 2; //??????????????????????????????
                    }
                }catch (Exception ex){
                    MI_FRESH_STATUS = 1;
                }
            }
            break;
            case 2:{
                // Judge if fail or not
            }
            break;
        }

    }

}