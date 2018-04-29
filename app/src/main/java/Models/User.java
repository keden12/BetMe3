package Models;

import java.util.Objects;


public class User {


    private long balance;
    private long bets;


    public User(long balance, long bets)
    {

        this.balance = balance;
        this.bets = bets;
    }



    public boolean equals(final Object obj)
    {
        if (obj instanceof User)
        {
            final User other = (User) obj;
            return Objects.equals(balance,  other.balance)
                    && Objects.equals(bets,  other.bets);

        }
        else
        {
            return false;
        }
    }







}
