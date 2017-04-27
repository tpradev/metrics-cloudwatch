/*
 * Copyright 2010-2014 Ning, Inc.
 * Copyright 2014-2015 The Billing Project, LLC
 *
 * The Billing Project licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.ning.billing.recurly.model;

import com.google.common.base.Objects;

import javax.xml.bind.annotation.XmlElement;

/**
 * Subscription object for update calls.
 * <p>
 * The timeframe parameter is specific to the update.
 */

/*
newly added fields:
    netTerms
    poNumber
    couponCode
    revenueScheduleType
    remainingBillingCycles
 */

public class SubscriptionUpdate extends AbstractSubscription {

    public static enum Timeframe {
        now,
        renewal
    }

    @XmlElement
    private Timeframe timeframe;

    public Timeframe getTimeframe() {
        return timeframe;
    }

    public void setTimeframe(final Timeframe timeframe) {
        this.timeframe = timeframe;
    }


    @XmlElement(name = "collection_method")
    private String collectionMethod;

    public String getCollectionMethod() {
        return collectionMethod;
    }

    public void setCollectionMethod(Object collectionMethod) {
        this.collectionMethod = stringOrNull(collectionMethod);
    }



    @XmlElement(name = "net_terms")
    private Integer netTerms;

    public Integer getNetTerms() {
        return netTerms;
    }

    public void setNetTerms(Integer netTerms) {
        this.netTerms = netTerms;
    }


    @XmlElement(name = "po_number")
    private String poNumber;

    public String getPoNumber() {
        return poNumber;
    }

    public void setPoNumber(String poNumber) {
        this.poNumber = poNumber;
    }



    @XmlElement(name = "coupon_code")
    private String couponCode;

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }



    @XmlElement(name = "revenue_schedule_type")
    private RevenueScheduleType revenueScheduleType;

    public RevenueScheduleType getRevenueScheduleType() {
        return revenueScheduleType;
    }

    public void setRevenueScheduleType(RevenueScheduleType revenueScheduleType) {
        this.revenueScheduleType = revenueScheduleType;
    }


    @XmlElement(name = "remaining_billing_cycles")
    private Integer remainingBillingCycles;

    public Integer getRemainingBillingCycles() {
        return remainingBillingCycles;
    }

    public void setRemainingBillingCycles(Integer remainingBillingCycles) {
        this.remainingBillingCycles = remainingBillingCycles;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final SubscriptionUpdate that = (SubscriptionUpdate) o;

        if (collectionMethod != null ? !collectionMethod.equals(that.collectionMethod) : that.collectionMethod != null) {
            return false;
        }
        if (timeframe != that.timeframe) {
            return false;
        }

        //newly added
        if (netTerms != null ? !netTerms.equals(that.netTerms) : that.netTerms != null) {
            return false;
        }
        if (collectionMethod != null ? !collectionMethod.equals(that.collectionMethod) : that.collectionMethod != null) {
            return false;
        }
        if (poNumber != null ? !poNumber.equals(that.poNumber) : that.poNumber != null) {
            return false;
        }
        if (couponCode != null ? !couponCode.equals(that.couponCode) : that.couponCode != null) {
            return false;
        }
        if (revenueScheduleType != null ? !revenueScheduleType.equals(that.revenueScheduleType) : that.revenueScheduleType != null) {
            return false;
        }
        if (remainingBillingCycles != null ? !remainingBillingCycles.equals(that.remainingBillingCycles) : that.remainingBillingCycles != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(
                timeframe,
                collectionMethod,

                //newly added
                netTerms,
                poNumber,
                couponCode,
                revenueScheduleType,
                remainingBillingCycles
        );
    }
}
