package com.ning.billing.recurly.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Created by farza on 4/24/17.
 */

@XmlRootElement(name = "account_acquisition")
public class AccountAcquisition extends RecurlyObject{


    @XmlTransient
    public static final String ACCOUNT_ACQUISITION_RESOURCE = "/acquisition";

    @XmlElement(name = "account_code")
    private String accountCode;

    @XmlElement(name = "cost_in_cents")
    Integer costInCents;

    @XmlElement(name = "currency")
    com.ning.billing.recurly.model.Currency currency;

    @XmlElement(name = "channel")
    Channel channel;

    @XmlElement(name = "subchannel")
    String subchannel;

    @XmlElement(name = "campaign")
    String campaign;

    public String getAccountCode() {
        return accountCode;
    }

    public void setAccountCode(final Object accountCode) {
        this.accountCode = stringOrNull(accountCode);
    }

    public Integer getCostInCents(){
        return costInCents;
    }

    public void setCostInCents(final Object costInCents){
        this.costInCents = integerOrNull(costInCents);
    }

    public com.ning.billing.recurly.model.Currency getCurrency(){
        return currency;
    }

    public void setCurrency(final com.ning.billing.recurly.model.Currency currency){
        this.currency = currency;
    }

    public Channel getChannel(){
        return channel;
    }

    public void setChannel(final Channel channel){
        this.channel =  channel;
    }

    public String getSubchannel(){
        return subchannel;
    }

    public void setSubchannel(final Object subchannel){
        this.subchannel = stringOrNull(subchannel);
    }

    public String getCampaign() {
        return campaign;
    }

    public void setCampaign(final Object campaign) {
        this.campaign = stringOrNull(campaign);
    }

    //check if toString needs to be implemented with string builder


    @Override
    public String toString() {
        return "AccountAcquisition{" +
                "accountCode='" + accountCode + '\'' +
                ", costInCents=" + costInCents +
                ", currency=" + currency +
                ", channel=" + channel +
                ", subchannel='" + subchannel + '\'' +
                ", campaign='" + campaign + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        AccountAcquisition that = (AccountAcquisition) o;

        if (accountCode != null ? !accountCode.equals(that.accountCode) : that.accountCode != null) return false;
        if (costInCents != null ? !costInCents.equals(that.costInCents) : that.costInCents != null) return false;
        if (currency != that.currency) return false;
        if (channel != that.channel) return false;
        if (subchannel != null ? !subchannel.equals(that.subchannel) : that.subchannel != null) return false;
        return campaign != null ? campaign.equals(that.campaign) : that.campaign == null;
    }


}
