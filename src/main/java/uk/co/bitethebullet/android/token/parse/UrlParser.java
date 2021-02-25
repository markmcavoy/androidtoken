package uk.co.bitethebullet.android.token.parse;


import uk.co.bitethebullet.android.token.tokens.ITokenMeta;
import uk.co.bitethebullet.android.token.tokens.TokenMetaData;

public class UrlParser {

    public static ITokenMeta parseOtpAuthUrl(String url) throws OtpAuthUriException {

        int tokenType;
        String tokenName;
        String organisation = null;
        String secret = null;
        int digits = 6;
        int counter = 0;
        int period = 30;
        boolean hasCounterParameter = false;

        if(!url.startsWith("otpauth://")){
            //not a valid otpauth url
            //throw new OtpAuthUriException(context.getString(R.string.otpAuthUrlInvalid));
            throw new OtpAuthUriException();
        }

        String tokenTypeString = url.substring(10, url.indexOf("/", 10));

        if(tokenTypeString.equals("hotp")){
            tokenType = TokenMetaData.HOTP_TOKEN;
        }else if(tokenTypeString.equals("totp")){
            tokenType = TokenMetaData.TOTP_TOKEN;
        }else{
            //the token type parameter is not valid
            //throw new OtpAuthUriException(context.getString(R.string.otpAuthTokenTypeInvalid));
            throw new OtpAuthUriException();
        }

        tokenName = url.substring(url.indexOf("/", 10) + 1, url.indexOf("?", 10));

        //decode url
        tokenName = java.net.URLDecoder.decode(tokenName);

        //check to see if we have an organisation prefix for the token name
        if(tokenName.contains(":")){
            organisation = tokenName.substring(0, tokenName.indexOf(":")).trim();
            tokenName = tokenName.substring(tokenName.indexOf(":") + 1).trim();
        }
        String[] parameters = url.substring(url.indexOf("?") + 1).split("&");

        for(int i = 0; i < parameters.length; i++){
            String[] paraDetail = new String[2];

            //get the key
            paraDetail[0] = parameters[i].substring(0, parameters[i].indexOf("="));

            //get the value
            paraDetail[1] = parameters[i].substring(parameters[i].indexOf("=") + 1, parameters[i].length());

            //read the parameter and work out if its
            //a valid parameter, if not just ignore
            if(paraDetail[0].equals("secret")){
                secret = paraDetail[1];
            }else if(paraDetail[0].equals("digits")){
                digits = Integer.parseInt(paraDetail[1]);
            }else if(paraDetail[0].equals("counter")){
                counter = Integer.parseInt(paraDetail[1]);
                hasCounterParameter = true;
            }else if(paraDetail[0].equals("period")){
                period = Integer.parseInt(paraDetail[1]);
            }
        }

        if(tokenType == TokenMetaData.HOTP_TOKEN && !hasCounterParameter){
            //when the token is a hotp token it must have the counter
            //parameter supplied otherwise we should throw an error
            //throw new OtpAuthUriException(context.getString(R.string.otpAuthMissingCounterParameter));
            throw new OtpAuthUriException();
        }

        return new TokenMetaData(tokenName, tokenType, secret, digits, period, counter, organisation);
    }
}
