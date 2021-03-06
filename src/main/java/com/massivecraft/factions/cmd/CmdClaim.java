package com.massivecraft.factions.cmd;

import com.massivecraft.factions.Conf;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.util.SpiralTask;

public class CmdClaim extends FCommand {

    public CmdClaim() {
        super();
        aliases.add("claim");

        // this.requiredArgs.add("");
        optionalArgs.put("faction", "your");
        optionalArgs.put("radius", "1");

        permission = Permission.CLAIM.node;
        disableOnLock = true;

        senderMustBePlayer = true;
        senderMustBeMember = false;
        senderMustBeOfficer = false;
        senderMustBeLeader = false;
    }

    @Override
    public void perform() {
        // Read and validate input
        final Faction forFaction = this.argAsFaction(0, myFaction);
        int radius = this.argAsInt(1, 1);

        if (radius < 1) {
            msg("<b>If you specify a radius, it must be at least 1.");
            return;
        }

        if (radius < 2) {
            // single chunk
            fme.attemptClaim(forFaction, me.getLocation(), true);
        } else {
            // radius claim
            if (!Permission.CLAIM_RADIUS.has(sender, false)) {
                msg("<b>You do not have permission to claim in a radius.");
                return;
            }

            new SpiralTask(new FLocation(me), radius) {
                private int failCount = 0;
                private final int limit = Conf.radiusClaimFailureLimit - 1;

                @Override
                public boolean work() {
                    boolean success = fme.attemptClaim(forFaction, currentLocation(), true);
                    if (success) {
                        failCount = 0;
                    } else if (!success && failCount++ >= limit) {
                        stop();
                        return false;
                    }

                    return true;
                }
            };
        }
    }

}
