package uk.co.bitethebullet.android.token.tokens;

import android.content.Context;

import uk.co.bitethebullet.android.token.R;
import uk.co.bitethebullet.android.token.util.FontManager;


/**
 * Class that will suggest the icon to use for a given token
 * this will look at the tokens organisation and attempt to
 * find a suitable match in the brands we define
 */
public class IconSuggestor {

    static String[] icons = {"fabAmazon", "fabApple", "fabAtlassian", "fabAws", "fabBitbucket",
                            "fabEbay", "fabEtsy", "fabEvernote", "fabFacebook", "fabGithub",
                            "fabGoogle", "fabInstagram", "fabJira", "fabMicrosoft", "fabPaypal",
                            "fabSaleforce", "fabSnapchat", "fabSquarespace", "fabStripe",
                            "fabTrello", "fabWordpress"};


    /**
     * work out the icon we can use for the this token
     * @param token
     * @param context
     * @return
     */
    public IconResult getSuggestedIcon(IToken token, Context context){

        IconResult result = null;

        String org = token.getOrganisation();
        int tokenType = token.getTokenType();

        if(org != null && org.length() > 0){
            org = org.toLowerCase();

            for (int i = 0; i < icons.length; i++){
                String iconMatch = icons[i].substring(3).toLowerCase();

                if(org.contains(iconMatch)){
                    result = new IconResult(FontManager.FONTAWESOME_BRANDS,
                                                getStringResource(context, icons[i]));

                    return result;
                }
            }

        }

        //if we get here we've not found anything to
        //match the organisation on
        //just show an icon based on the token type
        if(tokenType == TokenMetaData.HOTP_TOKEN){
            result = new IconResult(FontManager.FONTAWESOME,
                                    context.getString(R.string.farPlusSquare));
        }else{
            result = new IconResult(FontManager.FONTAWESOME,
                                    context.getString(R.string.farClock));
        }

        return result;
    }

    private static String getStringResource(Context context, String name) {
        int stringId = context.getResources().getIdentifier(name, "string", context.getPackageName());

        return context.getString(stringId);
    }

    /**
     * the result of the suggested icon to use
     */
    public class IconResult {

        private String _fontname;
        private String _content;

        public IconResult(String fontName, String content){
            this._fontname = fontName;
            this._content = content;
        }

        public String getFont(){
            return _fontname;
        }

        public String getContent(){
            return _content;
        }

    }
}
