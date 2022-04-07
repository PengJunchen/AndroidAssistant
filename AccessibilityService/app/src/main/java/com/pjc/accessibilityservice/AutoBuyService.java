package com.pjc.accessibilityservice;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

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
                    if (parentNode.getChild(count - 1).getText().length() > 0) {
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
                String className = event.getClassName().toString();
                if (className.equalsIgnoreCase("com.yaya.zone.activity.HomeActivity")){
                    // Goto shopping cart
                    if (findNodeListByTextParentTwiceClick(event, DING_DONG_SHOPPINGCART_TEXT)){
                        DING_DONG_STATUS = 1; //执行成功进入购物车
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
            }
            break;
            default:
            break;
        }

        deplay();
    }

    @Override
    public void onInterrupt() {

    }
}