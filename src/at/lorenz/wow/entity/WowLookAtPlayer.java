package at.lorenz.wow.entity;


import at.lorenz.wow.WowUser;
import at.lorenz.wow.UserState;
import at.lorenz.wow.WoWPlugin;
import net.minecraft.server.v1_13_R2.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class WowLookAtPlayer extends PathfinderGoal {
    protected EntityInsentient a;
    protected EntityLiving b;
    protected float c;
    private int e;
    private final float f;
    protected Class<? extends Entity> d;
    private WoWPlugin plugin;

    public WowLookAtPlayer(WoWPlugin plugin, EntityInsentient var0, Class<? extends EntityHuman> var1, float var2) {
        this(var0, (Class<? extends Entity>) var1, var2, 0.02F);
        this.plugin = plugin;
    }

    public WowLookAtPlayer(EntityInsentient var0, Class<? extends Entity> var1, float var2, float var3) {
        this.a = var0;
        this.d = var1;
        this.c = var2;
        this.f = var3;
        this.a(2);
    }

    public boolean a() {
        if (this.a.getRandom().nextFloat() >= this.f) {
            return false;
        } else {
            if (this.a.getGoalTarget() != null) {
                this.b = this.a.getGoalTarget();
            }
            if (b instanceof Player) {
                Player player = (Player) this.b;
                WowUser user = plugin.getUserManager().getUser(player);
                if (user.getUserState() == UserState.INGAME) {
                    this.b = this.a.world.a(this.a.locX, this.a.locY, this.a.locZ, (double) this.c, IEntitySelector.f.and(IEntitySelector.b(this.a)));
                } else {
                    this.b = null;
                }
            }
            return this.b != null;
        }
    }

    public boolean b() {
        if (!this.b.isAlive()) {
            return false;
        } else if (this.a.h(this.b) > (double) (this.c * this.c)) {
            return false;
        } else {
            return this.e > 0;
        }
    }

    public void c() {
        this.e = 40 + this.a.getRandom().nextInt(40);
    }

    public void d() {
        this.b = null;
    }

    public void e() {
        this.a.getControllerLook().a(this.b.locX, this.b.locY + (double) this.b.getHeadHeight(), this.b.locZ, (float) this.a.L(), (float) this.a.K());
        --this.e;
    }
}

