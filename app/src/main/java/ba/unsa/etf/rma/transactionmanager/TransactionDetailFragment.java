package ba.unsa.etf.rma.transactionmanager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Movie;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class TransactionDetailFragment extends Fragment {
    private EditText titleEditText;
    private EditText dateEditText;
    private EditText amountEditText;
    private EditText typeEditText;
    private EditText descriptionEditText;
    private EditText endDateEditText;
    private EditText intervalEditText;
    private Button saveBtn;
    private Button deleteBtn;
    private Transaction transaction;
    private Transaction transactionParc;
    private ArrayList<Transaction> transactions;
    private boolean titleVal = true, dateVal = true, amountVal = true, typeVal = true, descriptionVal = true,
            endDateVal = true, intervalVal = true, savetrigger = false;
    private double monthSpent = 0.0;
    private double totalSpent = 0.0;
    private final String[] typeArray = { "All", "INDIVIDUALPAYMENT", "REGULARPAYMENT", "PURCHASE", "INDIVIDUALINCOME",
            "REGULARINCOME"};
    private boolean eOa;
    private OnDelete oid;
    private OnRefresh or;

    private ITransactionDetailPresenter presenter;

    public interface OnDelete {
        void onItemDeleted(Transaction transaction);
    }

    public interface OnRefresh {
        void refreshFragment();
    }



    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_detail, container, false);

            titleEditText = (EditText) view.findViewById(R.id.titleEditText);
            dateEditText = (EditText) view.findViewById(R.id.dateEditText);
            amountEditText = (EditText) view.findViewById(R.id.amountEditText);
            typeEditText = (EditText) view.findViewById(R.id.typeEditText);
            descriptionEditText = (EditText) view.findViewById(R.id.descriptionEditText);
            endDateEditText = (EditText) view.findViewById(R.id.endDateEditText);
            intervalEditText = (EditText) view.findViewById(R.id.intervalEditText);
            saveBtn = (Button) view.findViewById(R.id.saveBtn);
            deleteBtn = (Button) view.findViewById(R.id.deleteBtn);

            if (getArguments() != null && getArguments().containsKey("editOrAdd")) {
                eOa = getArguments().getBoolean("editOrAdd");
            }

            if (getArguments() != null && getArguments().containsKey("transaction")) {
                transactionParc = (getArguments().getParcelable("transaction"));
                titleEditText.setText(transactionParc.getTitle());
                dateEditText.setText(transactionParc.getDate().toString());
                amountEditText.setText(String.valueOf(transactionParc.getAmount()));
                typeEditText.setText(transactionParc.getType().toString());
                descriptionEditText.setText(transactionParc.getItemDescription());
                if(transactionParc.getEndDate() != null)
                    endDateEditText.setText(transactionParc.getEndDate().toString());
                intervalEditText.setText(String.valueOf(transactionParc.getTransactionInterval()));


                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

                final String receivedTitle = transactionParc.getTitle();
                final String receivedDate = formatter.format(transactionParc.getDate());
                final String receivedAmount = String.valueOf(transactionParc.getAmount());
                final String receivedType = transactionParc.getType().toString();
                String receivedDescription = transactionParc.getItemDescription();
                String receivedEndDate = "";
                if(transactionParc.getEndDate() != null) {
                    receivedEndDate = formatter.format(transactionParc.getEndDate());
                }
                String receivedInterval = String.valueOf(transactionParc.getTransactionInterval());


                titleEditText.setText(receivedTitle);
                dateEditText.setText(receivedDate);
                amountEditText.setText(receivedAmount);
                typeEditText.setText(receivedType);
                descriptionEditText.setText(receivedDescription);
                endDateEditText.setText(receivedEndDate);
                intervalEditText.setText(receivedInterval);



            }


        try {
            oid = (OnDelete) getActivity();
        } catch (ClassCastException e) {

            throw new ClassCastException(getActivity().toString() +
                    "Treba implementirati OnItemDelete");
        }
        try {
            or = (OnRefresh) getActivity();
        } catch (ClassCastException e) {

            throw new ClassCastException(getActivity().toString() +
                    "Treba implementirati OnItemEdited");
        }

        //Disable delete button if user chose to add transaction
        if(eOa == true) {
            deleteBtn.setEnabled(false);
            dateVal = false;
        }
        //Disable end date and interval if type is not a regular one
        if (!typeEditText.getText().toString().equals("REGULARINCOME") &&
                !typeEditText.getText().toString().equals("REGULARPAYMENT")){
            endDateEditText.setEnabled(false);
            endDateEditText.setText("This type has no end date.");
            intervalEditText.setEnabled(false);
            intervalEditText.setText("0");
            endDateEditText.setBackgroundColor(Color.parseColor("#169617"));
            intervalEditText.setBackgroundColor(Color.parseColor("#169617"));
            endDateVal = true;
            intervalVal = true;
        }

        //EditText Listeners
        titleEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.length() < 4 || charSequence.length() > 15) {
                    titleEditText.setBackgroundColor(Color.parseColor("#B41D1D"));
                    titleVal = false;
                }
                else if(charSequence.length() > 0) {
                    titleEditText.setBackgroundColor(Color.parseColor("#169617"));
                    titleVal = true;
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        dateEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                boolean isDateValid = false;
                DateFormat formatOne = new SimpleDateFormat("dd/MM/yyyy");
                DateFormat formatTwo = new SimpleDateFormat("dd-MM-yyyy");

                try {
                    formatOne.parse(charSequence.toString());
                    isDateValid = true;
                } catch (ParseException e) {
                    try {
                        isDateValid = false;
                        formatTwo.parse(charSequence.toString());
                        isDateValid = true;
                    } catch (ParseException e2) {
                        //Already should be false
                    }
                }
                if(isDateValid) {
                    dateEditText.setBackgroundColor(Color.parseColor("#169617"));
                    dateVal = true;
                }
                else {
                    dateEditText.setBackgroundColor(Color.parseColor("#B41D1D"));
                    dateVal = false;
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        amountEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.toString().matches("-?\\d+(\\.\\d+)?")) {
                    amountEditText.setBackgroundColor(Color.parseColor("#169617"));
                    amountVal = true;
                }
                else {
                    amountEditText.setBackgroundColor(Color.parseColor("#B41D1D"));
                    amountVal = false;
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        typeEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                boolean isValid = false;
                for(String s: typeArray){
                    if(typeEditText.getText().toString().equals(s)) {
                        isValid = true;
                    }
                }
                if(transactionParc != null && typeEditText.getText().toString().equals(transactionParc.getType().toString()))
                    isValid = false;
                if(isValid) {
                    typeEditText.setBackgroundColor(Color.parseColor("#169617"));
                    typeVal = true;
                }
                else {
                    typeEditText.setBackgroundColor(Color.parseColor("#B41D1D"));
                    typeVal = false;
                }

                if (!typeEditText.getText().toString().equals("REGULARINCOME") &&
                        !typeEditText.getText().toString().equals("REGULARPAYMENT")) {
                    endDateEditText.setEnabled(false);
                    endDateEditText.setText("This type has no end date.");
                    intervalEditText.setEnabled(false);
                    intervalEditText.setText("0");
                    endDateEditText.setBackgroundColor(Color.parseColor("#169617"));
                    intervalEditText.setBackgroundColor(Color.parseColor("#169617"));
                    endDateVal = true;
                    intervalVal = true;
                }
                else{
                    endDateEditText.setEnabled(true);
                    endDateEditText.setText("");
                    intervalEditText.setEnabled(true);
                    intervalEditText.setText("");
                    endDateEditText.setBackgroundColor(Color.parseColor("#B41D1D"));
                    intervalEditText.setBackgroundColor(Color.parseColor("#B41D1D"));
                    endDateVal = false;
                    intervalVal = false;
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {


            }
        });
        descriptionEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.length() == 0) {
                    descriptionEditText.setBackgroundColor(Color.parseColor("#B41D1D"));
                    descriptionVal = false;
                }
                else if(charSequence.length() > 0) {
                    descriptionEditText.setBackgroundColor(Color.parseColor("#169617"));
                    descriptionVal = true;
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        endDateEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                boolean isDateValid = false;
                DateFormat formatOne = new SimpleDateFormat("dd/MM/yyyy");
                DateFormat formatTwo = new SimpleDateFormat("dd-MM-yyyy");

                if (typeEditText.getText().toString().equals("REGULARINCOME") ||
                        typeEditText.getText().toString().equals("REGULARPAYMENT")){
                    try {
                        formatOne.parse(charSequence.toString());
                        isDateValid = true;
                    } catch (ParseException e) {
                        try {
                            isDateValid = false;
                            formatTwo.parse(charSequence.toString());
                            isDateValid = true;
                        } catch (ParseException e2) {
                            //Already should be false
                        }
                    }
                    if (isDateValid) {
                        endDateEditText.setBackgroundColor(Color.parseColor("#169617"));
                        endDateVal = true;
                    }
                    else {
                        endDateEditText.setBackgroundColor(Color.parseColor("#B41D1D"));
                        endDateVal = false;
                    }
                }
                else{

                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        intervalEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.toString().matches("\\d+")) {
                    intervalEditText.setBackgroundColor(Color.parseColor("#169617"));
                    intervalVal = true;
                }
                else {
                    intervalEditText.setBackgroundColor(Color.parseColor("#B41D1D"));
                    intervalVal = false;
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(getActivity(), R.style.AlertDialog)
                        .setTitle("Delete transaction")
                        .setMessage("Are you sure you want to delete this transaction?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                                try {
                                    presenter = new TransactionDetailPresenter(getActivity());
                                    ((TransactionDetailPresenter) presenter).deleteTransaction(transactionParc);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                oid.onItemDeleted(transactionParc);

                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });


    saveBtn.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {


            if(!titleVal || !dateVal || !amountVal || !typeVal || !descriptionVal || !endDateVal || !intervalVal)
                {
                    savetrigger = false;
                    new AlertDialog.Builder(getActivity(), R.style.AlertDialog)
                            .setTitle("Changes")
                            .setMessage("Seems like some of your changes might be wrong.(Red color indicates an incorrect change).")
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
                else
                    savetrigger = true;

                if(savetrigger) {
                    try {

                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                        Transaction newTransaction = new Transaction();
                        newTransaction.setTitle(titleEditText.getText().toString());
                        newTransaction.setAmount(Double.valueOf(amountEditText.getText().toString()));
                        newTransaction.setItemDescription(descriptionEditText.getText().toString());
                        newTransaction.setDate(sdf.parse(dateEditText.getText().toString()));
                        if (typeEditText.getText().toString().equals("INDIVIDUALPAYMENT"))
                            newTransaction.setType(Transaction.Type.INDIVIDUALPAYMENT);
                        else if (typeEditText.getText().toString().equals("REGULARPAYMENT"))
                            newTransaction.setType(Transaction.Type.REGULARPAYMENT);
                        else if (typeEditText.getText().toString().equals("PURCHASE"))
                            newTransaction.setType(Transaction.Type.PURCHASE);
                        else if (typeEditText.getText().toString().equals("REGULARINCOME"))
                            newTransaction.setType(Transaction.Type.REGULARINCOME);
                        else if (typeEditText.getText().toString().equals("INDIVIDUALINCOME"))
                            newTransaction.setType(Transaction.Type.INDIVIDUALINCOME);
                        try {
                            if (intervalEditText.getText().toString().equals("0")) {
                                newTransaction.setEndDate(null);
                            } else
                                newTransaction.setEndDate(sdf.parse(endDateEditText.getText().toString()));
                            newTransaction.setTransactionInterval(Integer.valueOf(intervalEditText.getText().toString()));

                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        presenter = new TransactionDetailPresenter(getActivity());
                        if(eOa){
                            ((TransactionDetailPresenter) presenter).addTransaction(newTransaction);
                        }
                        else {
                            ((TransactionDetailPresenter) presenter).saveTransaction(transactionParc, newTransaction);
                        }
                        or.refreshFragment();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
        }
    });




        return view;
        }


}







//                saveBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if(!titleVal || !dateVal || !amountVal || !typeVal || !descriptionVal || !endDateVal || !intervalVal)
//                {
//                    savetrigger = false;
//                    new AlertDialog.Builder(getActivity(), R.style.AlertDialog)
//                            .setTitle("Changes")
//                            .setMessage("Seems like some of your changes might be wrong.(Red color indicates an incorrect change).")
//                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog, int which) {
//                                }
//                            })
//                            .setIcon(android.R.drawable.ic_dialog_alert)
//                            .show();
//                }
//                else
//                    savetrigger = true;
//
//                if(savetrigger) {
//
//                    double newAmount = 0;
//                    String text = amountEditText.getText().toString();
//                    if (!text.isEmpty())
//                        try {
//                            newAmount = Double.parseDouble(text);
//                        } catch (Exception e1) {
//
//                            e1.printStackTrace();
//                        }
//                    SimpleDateFormat sdf2 = new SimpleDateFormat("dd-MM-yyyy");
//                    Calendar calOne = Calendar.getInstance();
//                    Calendar calTwo = Calendar.getInstance();
//                    try {
//                        calTwo.setTime(sdf2.parse(dateEditText.getText().toString()));
//                        presenter = new TransactionDetailPresenter(getActivity());
//                        for (Transaction t : presenter.getInteractor().getTransactions()) {
//                            if (t.getType().toString().equals("PURCHASE") || t.getType().toString().equals("REGULARPAYMENT") ||
//                                    t.getType().toString().equals("INDIVIDUALPAYMENT")) {
//                                totalSpent = totalSpent + t.getAmount();
//                                calOne.setTime(t.getDate());
//                                int monthOne = calOne.get(Calendar.MONTH);
//                                int monthTwo = calTwo.get(Calendar.MONTH);
//                                if (monthOne == monthTwo)
//                                    monthSpent = monthSpent + t.getAmount();
//                            }
//                        }
//                        System.out.println("Total spent: " + totalSpent);
//                        System.out.println("Month spent: " + monthSpent);
//                    } catch (ParseException e) {
//                        e.printStackTrace();
//                    }
//
//
//                    String whatWentOver = "";
//                    if (newAmount + monthSpent > 5000.0)
//                        whatWentOver = "month";
//                    if (newAmount + totalSpent > 20000.0)
//                        whatWentOver = "global";
//                    if ((newAmount + totalSpent > 20000.0 || newAmount + monthSpent > 5000.0) &&
//                            (typeEditText.getText().toString().equals("INDIVIDUALPAYMENT") ||
//                                    typeEditText.getText().toString().equals("PURCHASE") ||
//                                    typeEditText.getText().toString().equals("REGULARPAYMENT"))) {
//                        new AlertDialog.Builder(getActivity(), R.style.AlertDialog)
//                                .setTitle("Over limit")
//                                .setMessage("This transaction went over your " + whatWentOver +
//                                        " limit. In this month you've spent: $" + monthSpent + " (limit is $5000) and " +
//                                        "in total you've spent: $" + totalSpent + " (limit is $20000)." +
//                                        "Are you sure you want to continue?")
//                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
//                                    public void onClick(DialogInterface dialog, int which) {
//                                        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
//                                        Transaction newTransaction = new Transaction();
//                                        newTransaction.setTitle(titleEditText.getText().toString());
//                                        newTransaction.setAmount(Double.valueOf(amountEditText.getText().toString()));
//                                        newTransaction.setItemDescription(descriptionEditText.getText().toString());
//                                        if (typeEditText.getText().toString().equals("INDIVIDUALPAYMENT"))
//                                            newTransaction.setType(Transaction.Type.INDIVIDUALPAYMENT);
//                                        else if (typeEditText.getText().toString().equals("REGULARPAYMENT"))
//                                            newTransaction.setType(Transaction.Type.REGULARPAYMENT);
//                                        else if (typeEditText.getText().toString().equals("PURCHASE"))
//                                            newTransaction.setType(Transaction.Type.PURCHASE);
//                                        else if (typeEditText.getText().toString().equals("REGULARINCOME"))
//                                            newTransaction.setType(Transaction.Type.REGULARINCOME);
//                                        else if (typeEditText.getText().toString().equals("INDIVIDUALINCOME"))
//                                            newTransaction.setType(Transaction.Type.INDIVIDUALINCOME);
//                                        try {
//                                            newTransaction.setDate(sdf.parse(dateEditText.getText().toString()));
//                                            if (intervalEditText.getText().toString().equals("0")) {
//                                                newTransaction.setEndDate(null);
//                                            } else
//                                                newTransaction.setEndDate(sdf.parse(endDateEditText.getText().toString()));
//                                            newTransaction.setTransactionInterval(Integer.valueOf(intervalEditText.getText().toString()));
//
//                                        } catch (ParseException e) {
//                                            e.printStackTrace();
//                                        }
//
//                                        if (eOa) {
//                                            //ADD Transaction
////                                            setResult(3, returnIntent);
////                                            try {
////                                                presenter = new TransactionDetailPresenter(ctx);
////                                                ((TransactionDetailPresenter) presenter).addTransaction(newTransaction);
////                                            } catch (ParseException e) {
////                                                e.printStackTrace();
////                                            }
////                                            finish();
//                                        } else {
//                                            try {
//                                                presenter = new TransactionDetailPresenter(getActivity());
//                                                ((TransactionDetailPresenter) presenter).saveTransaction(transactionParc, newTransaction);
//                                                oie.onItemEdited(transactionParc, newTransaction);
//                                            } catch (ParseException e) {
//                                                e.printStackTrace();
//                                            }
//                                        }
//
//                                        titleEditText.setBackgroundColor(Color.parseColor("#541068"));
//                                        dateEditText.setBackgroundColor(Color.parseColor("#541068"));
//                                        amountEditText.setBackgroundColor(Color.parseColor("#541068"));
//                                        typeEditText.setBackgroundColor(Color.parseColor("#541068"));
//                                        descriptionEditText.setBackgroundColor(Color.parseColor("#541068"));
//                                        endDateEditText.setBackgroundColor(Color.parseColor("#541068"));
//                                        intervalEditText.setBackgroundColor(Color.parseColor("#541068"));
//                                    }
//                                })
//                                .setNegativeButton(android.R.string.no, null)
//                                .setIcon(android.R.drawable.ic_dialog_alert)
//                                .show();
//                    } else {
//                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
//                        Transaction newTransaction = new Transaction();
//                        newTransaction.setTitle(titleEditText.getText().toString());
//                        try {
//                            newTransaction.setDate(sdf.parse(dateEditText.getText().toString()));
//                        } catch (ParseException e) {
//                            e.printStackTrace();
//                        }
//                        newTransaction.setAmount(Double.valueOf(amountEditText.getText().toString()));
//                        newTransaction.setItemDescription(descriptionEditText.getText().toString());
//                        if (typeEditText.getText().toString().equals("INDIVIDUALPAYMENT"))
//                            newTransaction.setType(Transaction.Type.INDIVIDUALPAYMENT);
//                        else if (typeEditText.getText().toString().equals("REGULARPAYMENT"))
//                            newTransaction.setType(Transaction.Type.REGULARPAYMENT);
//                        else if (typeEditText.getText().toString().equals("PURCHASE"))
//                            newTransaction.setType(Transaction.Type.PURCHASE);
//                        else if (typeEditText.getText().toString().equals("REGULARINCOME"))
//                            newTransaction.setType(Transaction.Type.REGULARINCOME);
//                        else if (typeEditText.getText().toString().equals("INDIVIDUALINCOME"))
//                            newTransaction.setType(Transaction.Type.INDIVIDUALINCOME);
//                        try {
//
//                            if (intervalEditText.getText().toString().equals("0")) {
//                                newTransaction.setEndDate(null);
//                            } else
//                                newTransaction.setEndDate(sdf.parse(endDateEditText.getText().toString()));
//                            newTransaction.setTransactionInterval(Integer.valueOf(intervalEditText.getText().toString()));
//
//                        } catch (ParseException e) {
//                            e.printStackTrace();
//                        }
//
//                        if (eOa) {
////                            setResult(3, returnIntent);
////                            try {
////                                presenter = new TransactionDetailPresenter(ctx);
////                                ((TransactionDetailPresenter) presenter).addTransaction(newTransaction);
////                            } catch (ParseException e) {
////                                e.printStackTrace();
////                            }
////                            finish();
//                        } else {
//                            try {
//                                presenter = new TransactionDetailPresenter(getActivity());
//                                ((TransactionDetailPresenter) presenter).saveTransaction(transactionParc, newTransaction);
//                                oie.onItemEdited(transactionParc, newTransaction);
//                            } catch (ParseException e) {
//                                e.printStackTrace();
//                            }
//                            titleEditText.setBackgroundColor(Color.parseColor("#541068"));
//                            dateEditText.setBackgroundColor(Color.parseColor("#541068"));
//                            amountEditText.setBackgroundColor(Color.parseColor("#541068"));
//                            typeEditText.setBackgroundColor(Color.parseColor("#541068"));
//                            descriptionEditText.setBackgroundColor(Color.parseColor("#541068"));
//                            endDateEditText.setBackgroundColor(Color.parseColor("#541068"));
//                            intervalEditText.setBackgroundColor(Color.parseColor("#541068"));
//                        }
//                    }
//                }
//                totalSpent = 0.0;
//                monthSpent = 0.0;
//            }
//        });