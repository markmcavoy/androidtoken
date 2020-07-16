package uk.co.bitethebullet.android.token.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import uk.co.bitethebullet.android.token.R;
import uk.co.bitethebullet.android.token.datalayer.TokenDbAdapter;
import uk.co.bitethebullet.android.token.tokens.IToken;
import uk.co.bitethebullet.android.token.tokens.IconSuggestor;
import uk.co.bitethebullet.android.token.tokens.TokenFactory;
import uk.co.bitethebullet.android.token.util.FontManager;

public class TokenAdapter extends BaseAdapter
{
    private Context mContext;
    private TokenDbAdapter mDbAdapter;
    private List<IToken> mTokens;

    public TokenAdapter(Context context, TokenDbAdapter dbAdapter){
        mContext = context;
        mDbAdapter = dbAdapter;

        Cursor cursor = mDbAdapter.fetchAllTokens();

        //read all the tokens we have and put them into a list
        //this will save hitting the db everytime we draw the
        //ui with an update

        mTokens = new ArrayList<IToken>();

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            mTokens.add(TokenFactory.CreateToken(cursor));
            cursor.moveToNext();
        }

        cursor.close();
    }

    public int getCount() {
        return mTokens.size();
    }

    public Object getItem(int position) {
        return mTokens.get(position);
    }

    public long getItemId(int position) {
        return mTokens.get(position).getId();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row =  inflater.inflate(R.layout.token_list_row, null);

        TextView nameText = (TextView)row.findViewById(R.id.tokenrowtextname);
        TextView serialText = (TextView)row.findViewById(R.id.tokenrowtextserial);
        TextView totpText = (TextView)row.findViewById(R.id.tokenRowTimeTokenOtp);

        TextView faIcon = (TextView)row.findViewById(R.id.faIcon);


        ProgressBar totpProgressBar = (ProgressBar)row.findViewById(R.id.totpTimerProgressbar);


        IToken currentToken = (IToken)getItem(position);

        //get an icon based on the organisation
        IconSuggestor iconSuggestor = new IconSuggestor();
        IconSuggestor.IconResult iconResult = iconSuggestor.getSuggestedIcon(currentToken, mContext);

        faIcon.setTypeface(FontManager.getTypeface(mContext, iconResult.getFont()));
        faIcon.setText(iconResult.getContent());

        if(currentToken.getOrganisation() == null){
            nameText.setText(currentToken.getName());
        }else{
            nameText.setText(currentToken.getName() + " (" + currentToken.getOrganisation() + ")");
        }

        if(currentToken.getSerialNumber().length() > 0)
            serialText.setText(currentToken.getSerialNumber());
        else{
            TextView serialCaption = (TextView)row.findViewById(R.id.tokenrowtextserialcaption);
            serialCaption.setVisibility(View.GONE);
            serialText.setVisibility(View.GONE);
        }

        //if the token is a time token, just display the current
        //value for the token. Event tokens will still need to
        //be click to display the otp
        if(currentToken.getTokenType() == TokenDbAdapter.TOKEN_TYPE_TIME){
            totpText.setVisibility(View.VISIBLE);
            totpText.setText(currentToken.generateOtp());

            totpProgressBar.setVisibility(View.VISIBLE);

            Date dt = new Date();
            float curSec = (float)dt.getSeconds();
            int progress;

            if(currentToken.getTimeStep() == 30){

                if(curSec > 30)
                    curSec = curSec - 30;

                progress = (int)(100 - ((curSec/30)*100));
            }else{
                progress = (int)(100 - ((curSec/60)*100));
            }

            totpProgressBar.setProgress(progress);

            //when we get to the last 5 seconds, lets change
            //the token colour to red as a warning
            if(progress > 10){
                totpText.setTextColor(nameText.getTextColors());
            }else{
                totpText.setTextColor(Color.RED);
            }
        }

        return row;
    }

}