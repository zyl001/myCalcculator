package cn.zyl.hello1;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.CompoundButton;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class MainActivity extends AppCompatActivity {
    //面额
    public enum Denominations{
        Hundred,
        Fifty,
        Twenty,
        Ten,
        Five,
        One
    }

    int [] numberArray;//每种面额对应的张数
    int [] valueArray;//每种面额对应的金额值
    boolean []checkedStateArray;
    static int iTotalCount = 0;//输入或反向计算出的总金额
    EditText [] editTextArray;
    Map<Integer,Integer> mapSwitchIndex;//switch控件id -- 面额类型枚举
    EditText editText_totalCount;
    int iHundred = 0;
    int iHundredMax = 0;
    int iFiftyMax = 0;
    int iFifty = 0;
    int iTwentyMax = 0;
    int iTwenty = 0;
    int iTenMax = 0;
    int iTen = 0;
    int iFiveMax = 0;
    int iFive = 0;
    int iOne = 0;
    int iCheckedCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText_totalCount = findViewById(R.id.editText_TotalCount);
        SwitchOnCheckedChangeListener checkedChangedHandler = new SwitchOnCheckedChangeListener();
        Switch switchBtn;
        switchBtn = findViewById(R.id.btnHundred);
        switchBtn.setOnCheckedChangeListener(checkedChangedHandler);
        switchBtn = findViewById(R.id.btnFifty);
        switchBtn.setOnCheckedChangeListener(checkedChangedHandler);
        switchBtn = findViewById(R.id.btnTwenty);
        switchBtn.setOnCheckedChangeListener(checkedChangedHandler);
        switchBtn = findViewById(R.id.btnTen);
        switchBtn.setOnCheckedChangeListener(checkedChangedHandler);
        switchBtn = findViewById(R.id.btnFive);
        switchBtn.setOnCheckedChangeListener(checkedChangedHandler);
        switchBtn = findViewById(R.id.btnOne);
        switchBtn.setOnCheckedChangeListener(checkedChangedHandler);
        init();
        Button btnCalc = findViewById(R.id.btn_calc);
        btnCalc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strTotalCount = editText_totalCount.getText().toString();
                boolean bValidateValue = true;
                bValidateValue = checkValidate(strTotalCount);
                if (!bValidateValue)
                {
                    return;
                }
                try {
                    iTotalCount =Integer.parseInt(strTotalCount);
                }
                catch (NumberFormatException e)
                {
                    Toast.makeText(MainActivity.this, "输入数据只能包含数字,不能包含其他字符和空格", Toast.LENGTH_SHORT).show();
                    return;
                }
                boolean bResult = false;
                if (iCheckedCount == 0)
                {
                    //processAllRandomFun(iTotalCount);
                    Vector<Integer> randomDenomation = checkRandomDenominations();
                    bResult = processPartialRandom(iTotalCount,randomDenomation);
                }
                else
                {
                    boolean result = false;
                    Vector<Integer> randomDenomination = checkRandomDenominations();
/*                    if(checkAbsoluteNoSolution(iTotalCount,randomDenomation))
                    {
                        Toast.makeText(MainActivity.this, "当前组合无解", Toast.LENGTH_SHORT).show();
                        return;
                    }*/
                    for (int i =0;i<100;++i)
                    {
                        result = processPartialRandom(iTotalCount,randomDenomination);
                        if (result)
                        {
                            bResult = true;
                            break;
                        }
                    }
                }

                if (bResult)
                {
                    showResult();
                }
                else
                {
                    Toast.makeText(MainActivity.this, "本次随机未找到合适组合，可再次尝试", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    protected void btnReset_clicked(View v){
        EditText editText;
        String strValue="";
        for (Denominations denomination: Denominations.values())
        {
            editText = editTextArray[denomination.ordinal()];
            editText.setText(strValue.toCharArray(),0,strValue.length());
            checkedStateArray[denomination.ordinal()] = false;
            numberArray[denomination.ordinal()] = 0;
        }
        for(Integer switchBtnId : mapSwitchIndex.keySet()){
            Switch switchBtn = findViewById(switchBtnId);
            switchBtn.setChecked(false);
        }
        iCheckedCount = 0;
        Toast.makeText(this, "清空成功", Toast.LENGTH_SHORT).show();
    }

    protected  void processAllRandomFun(int iTotalCount)
    {
        iHundredMax = iTotalCount/100;
        int iRemain = iTotalCount%100;
        iFiftyMax = iRemain/50;
        int iRemain50 = iRemain%50;
        iTwentyMax = iRemain50/20;
        int iRemain20 = iRemain50%20;
        iTenMax = iRemain20/10;
        int iRemain10 = iRemain20%10;
        iFiveMax = iRemain10/5;
        iOne = iRemain10%5;

        int iRandom = random(iHundredMax);
        iHundred = iHundredMax - iRandom;

        iFiftyMax += iRandom*2;
        iRandom = random(iFiftyMax);

        iFifty = iFiftyMax -iRandom;
        iTwentyMax+= iRandom*50/20;
        iTenMax+= iRandom*50%20/10;

        iRandom = random(iTwentyMax);
        iTwenty = iTwentyMax -iRandom;
        iTenMax += iRandom*2;

        iRandom = random(iTenMax);
        iTen = iTenMax - iRandom;
        iFiveMax += iRandom*2;

        iRandom = random(iFiveMax);

        iFive = iFiveMax -iRandom;
        iOne += iRandom*5;
    }

    //false计算无解,true计算成功
    private boolean processPartialRandom(int iTotalCount,Vector<Integer> randomDenominationIndexVec) {
        boolean result = false;
        int iAssignedTotalValue = 0;//已分配总值
        for(int i =0;i<checkedStateArray.length;++i)
        {
            if (checkedStateArray[i])
            {
                iAssignedTotalValue += valueArray[i]*numberArray[i];
            }
        }
        int iRemainTotal = iTotalCount - iAssignedTotalValue;//未分配剩余总值
        for (int i = 0;i<randomDenominationIndexVec.size();++i)
        {
            int iDenominationIndex =  randomDenominationIndexVec.get(i).intValue();
            int currentValue = valueArray[iDenominationIndex];

            if (i ==0 && i == randomDenominationIndexVec.size()-1) {
                int tempNum = iRemainTotal/currentValue;
                iRemainTotal = iRemainTotal % currentValue;
                if (iRemainTotal != 0) {
                    result = false;
                }
                else{
                    numberArray[iDenominationIndex] = tempNum;
                    result = true;
                }
            }
            else if (i == 0) {
                numberArray[iDenominationIndex] = iRemainTotal/currentValue;
                iRemainTotal = iRemainTotal % currentValue;
            }
            else if (i == randomDenominationIndexVec.size() -1) {
                int iLastDenominationIndex =  randomDenominationIndexVec.get(i-1).intValue();
                int randomNum = random(numberArray[iLastDenominationIndex]);
                numberArray[iLastDenominationIndex] -= randomNum;
                iRemainTotal += randomNum*valueArray[iLastDenominationIndex];

                int tempNum = iRemainTotal/currentValue;
                iRemainTotal = iRemainTotal%currentValue;
                if (iRemainTotal == 0 ) {
                    numberArray[iDenominationIndex] = tempNum;
                    result = true;
                }
                else {
                    result = false;
                }
            }
            else {
                int iLastDenominationIndex =  randomDenominationIndexVec.get(i-1).intValue();
                int randomNum = random(numberArray[iLastDenominationIndex]);
                numberArray[iLastDenominationIndex] -= randomNum;
                iRemainTotal += randomNum*valueArray[iLastDenominationIndex];
                numberArray[iDenominationIndex] = iRemainTotal/currentValue;
                iRemainTotal = iRemainTotal % currentValue;
            }
        }
        return result;
    }

    //true 绝对无解,false 可能有解
    private boolean checkAbsoluteNoSolution(int iTotalCount,Vector<Integer> randomDenominationIndexVec){
        int iSize = randomDenominationIndexVec.size();
        if ( iSize > 0 )
        {
            int iDenominationIndex =  randomDenominationIndexVec.get(iSize-1).intValue();
            int currentValue = valueArray[iDenominationIndex];
            if (iTotalCount%currentValue != 0){
                return true;
            }
        }
        return false;
    }

    private Vector<Integer> checkRandomDenominations(){
        Vector<Integer> randomDenominationIndexVec = new Vector<>();//待计算张数的面额向量
        for(int i =0;i<checkedStateArray.length;++i)
        {
            if (!checkedStateArray[i])
            {
                randomDenominationIndexVec.add(i);//面额大的在向量的前面
            }
        }
        return randomDenominationIndexVec;
    }

    protected void showResult(){
        EditText editText;
        String strValue;
        for (Denominations denomination: Denominations.values())
        {
            editText = editTextArray[denomination.ordinal()];
            strValue = Integer.toString(numberArray[denomination.ordinal()]);
            editText.setText(strValue.toCharArray(),0,strValue.length());
        }
/*        editText = findViewById(R.id.editText_hundred);
        strValue = Integer.toString(numberArray[Denominations.Hundred.ordinal()]);
        editText.setText(strValue.toCharArray(),0,strValue.length());
        editText = findViewById(R.id.editText_fifty);
        strValue = Integer.toString(iFifty);
        editText.setText(strValue.toCharArray(),0,strValue.length());
        editText = findViewById(R.id.editText_twenty);
        strValue = Integer.toString(iTwenty);
        editText.setText(strValue.toCharArray(),0,strValue.length());
        editText = findViewById(R.id.editText_ten);
        strValue = Integer.toString(iTen);
        editText.setText(strValue.toCharArray(),0,strValue.length());
        editText = findViewById(R.id.editText_five);
        strValue = Integer.toString(iFive);
        editText.setText(strValue.toCharArray(),0,strValue.length());
        editText = findViewById(R.id.editText_one);
        strValue = Integer.toString(iOne);
        editText.setText(strValue.toCharArray(),0,strValue.length());*/
    }
    private int random(int value){
        if (value == 0)
        {
            return 0;
        }
        else
        {
            long l = System.currentTimeMillis();
            int iRandom = (int)(l%(value/3+1));
            return iRandom;
        }
    }
    private boolean checkValidate(String strValue){
        boolean bValidateValue = true;
        String strMaxValue = Integer.toString(Integer.MAX_VALUE);

        do {
            if (strValue.length() > strMaxValue.length()) {
                bValidateValue = false;
                break;
            }
            else if (strValue.length() == strMaxValue.length()) {
                if (strValue.compareTo(strMaxValue) > 0) {
                    bValidateValue = false;
                    break;
                }
            }
            else if (strValue.length() == 0)
            {
                bValidateValue = false;
                break;
            }
        }while (false);

        if (!bValidateValue)
        {
            if (strValue.length() == 0)
            {
                Toast.makeText(this, "总额不能为空", Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(this, "最大值要小于21亿", Toast.LENGTH_SHORT).show();
            }
        }
        return  bValidateValue;
    }
    public boolean checkCurrentTotalValidate(){
        long currentTotal = 0;
        for (int index = 0;index<checkedStateArray.length;++index){
            if (checkedStateArray[index])
            {
                currentTotal += numberArray[index]*valueArray[index];
            }
        }
        if (currentTotal>iTotalCount)
        {
            return false;
        }
        else
        {
            return true;
        }
    }
    private void init(){
        numberArray = new int[Denominations.values().length];
        checkedStateArray = new boolean[Denominations.values().length];
        valueArray = new int[Denominations.values().length];
        valueArray[Denominations.Hundred.ordinal()] = 100;
        valueArray[Denominations.Fifty.ordinal()] = 50;
        valueArray[Denominations.Twenty.ordinal()] = 20;
        valueArray[Denominations.Ten.ordinal()] = 10;
        valueArray[Denominations.Five.ordinal()] = 5;
        valueArray[Denominations.One.ordinal()] = 1;

        editTextArray = new EditText[Denominations.values().length];
        editTextArray[Denominations.Hundred.ordinal()] = findViewById(R.id.editText_hundred);
        editTextArray[Denominations.Fifty.ordinal()] = findViewById(R.id.editText_fifty);
        editTextArray[Denominations.Twenty.ordinal()] = findViewById(R.id.editText_twenty);
        editTextArray[Denominations.Ten.ordinal()] = findViewById(R.id.editText_ten);
        editTextArray[Denominations.Five.ordinal()] = findViewById(R.id.editText_five);
        editTextArray[Denominations.One.ordinal()] = findViewById(R.id.editText_one);


        mapSwitchIndex = new HashMap<Integer, Integer>();
        mapSwitchIndex.put(R.id.btnHundred,Denominations.Hundred.ordinal());
        mapSwitchIndex.put(R.id.btnFifty,Denominations.Fifty.ordinal());
        mapSwitchIndex.put(R.id.btnTwenty,Denominations.Twenty.ordinal());
        mapSwitchIndex.put(R.id.btnTen,Denominations.Ten.ordinal());
        mapSwitchIndex.put(R.id.btnFive,Denominations.Five.ordinal());
        mapSwitchIndex.put(R.id.btnOne,Denominations.One.ordinal());
    }

    private boolean handleSwitchBtnChecked(CompoundButton button, boolean bChecked){
        int btnId = button.getId();
        int iValue = mapSwitchIndex.get(btnId);
        Denominations denomination =  Denominations.values()[iValue];
        int index = denomination.ordinal();
        if (bChecked)
        {
            String strTotalCount = editText_totalCount.getText().toString();
            if(!checkValidate(strTotalCount))
            {
                button.setChecked(false);
                return false;
            }
            String strValue = editTextArray[index].getText().toString();
            if (strValue.isEmpty())
            {
                Toast.makeText(this, "当前值不能为空", Toast.LENGTH_SHORT).show();
                button.setChecked(false);
                return false;
            }
            int num = Integer.parseInt(strValue);
            numberArray[index] = num;
            checkedStateArray[index] = true;
            if (!checkCurrentTotalValidate())
            {
                checkedStateArray[index] = false;
                return false;
            }
            iCheckedCount++;
        }
        else
        {
            checkedStateArray[index] = false;
            iCheckedCount--;
        }
        return true;
    }

    private class SwitchOnCheckedChangeListener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton button, boolean isChecked) {
            if (!(button instanceof Switch)) {
                return;
            }
            handleSwitchBtnChecked(button, isChecked);
        }
    }
}
