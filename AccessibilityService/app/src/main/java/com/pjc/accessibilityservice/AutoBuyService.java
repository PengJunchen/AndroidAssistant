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
    // 叮咚独立处理逻辑-------------------------------------------------------------------------------------
    private void dingdongPayingEffective(AccessibilityEvent event){
        // 如果不有效则需要回退

        // com.yaya.zone:id/tv_no_coupon_sum_money 商品金额
        try {
            List<AccessibilityNodeInfo> accessibilityNodeInfoList = findNodeListByText(event, "商品金额");
            if (accessibilityNodeInfoList != null && !accessibilityNodeInfoList.isEmpty()) {
                AccessibilityNodeInfo parentNode = accessibilityNodeInfoList.get(0).getParent();
                int count = parentNode.getChildCount();
                while (count > 0) {
                    if (parentNode.getChild(count -1).getText() != null && parentNode.getChild(count - 1).getText().length() > 0) {
                        if (findNodeListByTextAndClick(event, DING_DONG_PAY_TEXT)) {
                            DING_DONG_STATUS = 3;  //执行成功进入选择时间界面
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

    private String DING_DONG_SHOPPINGCART_TEXT = "购物车";
    private String DING_DONG_CHECKIN_TEXT = "去结算";
    private String DING_DONG_PAY_TEXT = "立即支付";

    private int DING_DONG_STATUS = -1;

    private void handleDingDong(AccessibilityEvent event){

        if (event == null || event.getSource() == null){
            return;
        }
        // 判断在哪一个页面！！
        if (findNodeListByText(event, DING_DONG_CHECKIN_TEXT) != null){
            DING_DONG_STATUS = 1;
        }
        if (findNodeListByText(event, DING_DONG_PAY_TEXT) != null){
            DING_DONG_STATUS = 2;
        }

        switch (DING_DONG_STATUS){
            case -1:{
                // 初始状态有可能因为载入问题没有正确获取，这是一个临时方案不一定有效
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
                        DING_DONG_STATUS = 1; //执行成功进入购物车
                    }
                }
                else {
                    String className = event.getClassName().toString();
                    if (className.equalsIgnoreCase("com.yaya.zone.activity.HomeActivity")) {
                        // Goto shopping cart
                        if (findNodeListByTextParentTwiceClick(event, DING_DONG_SHOPPINGCART_TEXT)) {
                            DING_DONG_STATUS = 1; //执行成功进入购物车
                        }
                    }
                }
            }
            break;
            case 1: {
                if (findNodeListByTextAndClick(event, DING_DONG_CHECKIN_TEXT)){
                    DING_DONG_STATUS = 2; //执行成功进入支付界面
                }
            }
            break;
            case 2: {
                // 判断支付有效性
                dingdongPayingEffective(event);
            }
            break;
            case 3: {
                // 判断时间有效性
                // 叮咚已经是自适应时间段 本段落逻辑未完善
                DING_DONG_STATUS = 2;
                Toast.makeText(getApplicationContext(), "Pay the money!", Toast.LENGTH_LONG).show();
            }
            break;
            default:
            break;
        }

        deplay();
    }

    // 盒马独立处理逻辑 --------------------------------------------------------------------------------------
    private String HIPPO_SHOPPINGCART_TEXT = "购物车";
    private String HIPPO_CHECKIN_TEXT = "结算";
    private String HIPPO_PAY_TEXT = "提交订单";


    private int HIPPO_STATUS = -1;

    private void handleHippo(AccessibilityEvent event){

        if (event == null || event.getSource() == null){
            return;
        }
        // 判断在哪一个页面！！
        if (findNodeListByText(event, HIPPO_CHECKIN_TEXT) != null){
            HIPPO_STATUS = 1;
        }

        switch (HIPPO_STATUS){
            case -1:{
                // 初始状态有可能因为载入问题没有正确获取，这是一个临时方案不一定有效
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
                    HIPPO_STATUS = 1; //执行成功进入购物车
                }
            }
            break;
            case 1: {
                if (findNodeListByTextAndClick(event, HIPPO_CHECKIN_TEXT)){
                    HIPPO_STATUS = 2; //执行成功进入支付界面
                }
            }
            break;
            case 2: {
                // 判断支付有效性
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

    // 每日优鲜独立处理逻辑 Not Complete as should search the whole ui tree to check content description --------------------------------------------------------------------------------------
    private String MI_FRESH_SHOPPINGCART_TEXT = "购物车";
    private String MI_FRESH_PAY_TEXT = "立即支付";
    private String MI_FRESH_FAILURE = "知道了";

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
                        MI_FRESH_STATUS = 1; //执行成功进入购物车
                    }
                }else {
                    String className = event.getClassName().toString();
                    if (className.equalsIgnoreCase("cn.missfresh.module.main.view.MainActivity")) {
                        // Goto shopping cart
                        if (findNodeListByTextParentClick(event, MI_FRESH_SHOPPINGCART_TEXT)) {
                            MI_FRESH_STATUS = 1; //执行成功进入购物车
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
                        MI_FRESH_STATUS = 2; //执行成功进入支付界面
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