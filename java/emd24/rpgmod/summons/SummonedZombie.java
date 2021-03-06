package emd24.rpgmod.summons;

import java.util.Calendar;
import java.util.UUID;

import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAIBreakDoor;
import net.minecraft.entity.ai.EntityAIFollowOwner;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMoveThroughVillage;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIOwnerHurtByTarget;
import net.minecraft.entity.ai.EntityAIOwnerHurtTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.living.ZombieEvent.SummonAidEvent;

public class SummonedZombie extends EntityTameable{

	protected static final IAttribute field_110186_bp = (new RangedAttribute("zombie.spawnReinforcements", 0.0D, 0.0D, 1.0D)).setDescription("Spawn Reinforcements Chance");
    private static final UUID babySpeedBoostUUID = UUID.fromString("B9766B59-9566-4402-BC1F-2EE2A276D836");
    private static final AttributeModifier babySpeedBoostModifier = new AttributeModifier(babySpeedBoostUUID, "Baby speed boost", 0.5D, 1);
    private final EntityAIBreakDoor field_146075_bs = new EntityAIBreakDoor(this);
    /**
     * Ticker used to determine the time remaining for this zombie to convert into a villager when cured.
     */
    private int conversionTime;
    private boolean field_146076_bu = false;
    private float field_146074_bv = -1.0F;
    private float field_146073_bw;
    private static final String __OBFID = "CL_00001702";
	
	public SummonedZombie(World par1World) {
		super(par1World);
        this.getNavigator().setBreakDoors(true);
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(2, new EntityAIAttackOnCollide(this, 1.0D, true));
        this.tasks.addTask(4, new EntityAIFollowOwner(this, 1.0D, 10.0F, 2.0F));
        this.tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, 1.0D));
        this.tasks.addTask(6, new EntityAIMoveThroughVillage(this, 1.0D, false));
        this.tasks.addTask(7, new EntityAIWander(this, 1.0D));
        this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(8, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new EntityAIOwnerHurtByTarget(this));
        this.targetTasks.addTask(2, new EntityAIOwnerHurtTarget(this));
        this.targetTasks.addTask(3, new EntityAIHurtByTarget(this, true));
        this.setSize(0.6F, 1.8F);
	}
	
	 protected void applyEntityAttributes()
	    {
	        super.applyEntityAttributes();
	        this.getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(40.0D);
	        this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.23000000417232513D);
	        this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(3.0D);
	        this.getAttributeMap().registerAttribute(field_110186_bp).setBaseValue(this.rand.nextDouble() * ForgeModContainer.zombieSummonBaseChance);
	    }

	    protected void entityInit()
	    {
	        super.entityInit();
	        this.getDataWatcher().addObject(12, Byte.valueOf((byte)0));
	        this.getDataWatcher().addObject(13, Byte.valueOf((byte)0));
	        this.getDataWatcher().addObject(14, Byte.valueOf((byte)0));
	    }

	    /**
	     * Returns the current armor value as determined by a call to InventoryPlayer.getTotalArmorValue
	     */
	    public int getTotalArmorValue()
	    {
	        int i = super.getTotalArmorValue() + 2;

	        if (i > 20)
	        {
	            i = 20;
	        }

	        return i;
	    }

	    /**
	     * Returns true if the newer Entity AI code should be run
	     */
	    protected boolean isAIEnabled()
	    {
	        return true;
	    }

	    public boolean func_146072_bX()
	    {
	        return this.field_146076_bu;
	    }

	    public void func_146070_a(boolean p_146070_1_)
	    {
	        if (this.field_146076_bu != p_146070_1_)
	        {
	            this.field_146076_bu = p_146070_1_;

	            if (p_146070_1_)
	            {
	                this.tasks.addTask(1, this.field_146075_bs);
	            }
	            else
	            {
	                this.tasks.removeTask(this.field_146075_bs);
	            }
	        }
	    }

	    /**
	     * If Animal, checks if the age timer is negative
	     */
	    public boolean isChild()
	    {
	        return this.getDataWatcher().getWatchableObjectByte(12) == 1;
	    }

	    /**
	     * Get the experience points the entity currently has.
	     */
	    protected int getExperiencePoints(EntityPlayer par1EntityPlayer)
	    {
	        if (this.isChild())
	        {
	            this.experienceValue = (int)((float)this.experienceValue * 2.5F);
	        }

	        return super.getExperiencePoints(par1EntityPlayer);
	    }

	    /**
	     * Set whether this zombie is a child.
	     */
	    public void setChild(boolean par1)
	    {
	        this.getDataWatcher().updateObject(12, Byte.valueOf((byte)(par1 ? 1 : 0)));

	        if (this.worldObj != null && !this.worldObj.isRemote)
	        {
	            IAttributeInstance iattributeinstance = this.getEntityAttribute(SharedMonsterAttributes.movementSpeed);
	            iattributeinstance.removeModifier(babySpeedBoostModifier);

	            if (par1)
	            {
	                iattributeinstance.applyModifier(babySpeedBoostModifier);
	            }
	        }

	        this.func_146071_k(par1);
	    }

	    /**
	     * Return whether this zombie is a villager.
	     */
	    public boolean isVillager()
	    {
	        return this.getDataWatcher().getWatchableObjectByte(13) == 1;
	    }

	    /**
	     * Set whether this zombie is a villager.
	     */
	    public void setVillager(boolean par1)
	    {
	        this.getDataWatcher().updateObject(13, Byte.valueOf((byte)(par1 ? 1 : 0)));
	    }

	    /**
	     * Called frequently so the entity can update its state every tick as required. For example, zombies and skeletons
	     * use this to react to sunlight and start to burn.
	     */
	    public void onLivingUpdate()
	    {
	        if (this.worldObj.isDaytime() && !this.worldObj.isRemote && !this.isChild())
	        {
	            float f = this.getBrightness(1.0F);

	            if (f > 0.5F && this.rand.nextFloat() * 30.0F < (f - 0.4F) * 2.0F && this.worldObj.canBlockSeeTheSky(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY), MathHelper.floor_double(this.posZ)))
	            {
	                boolean flag = true;
	                ItemStack itemstack = this.getEquipmentInSlot(4);

	                if (itemstack != null)
	                {
	                    if (itemstack.isItemStackDamageable())
	                    {
	                        itemstack.setItemDamage(itemstack.getItemDamageForDisplay() + this.rand.nextInt(2));

	                        if (itemstack.getItemDamageForDisplay() >= itemstack.getMaxDamage())
	                        {
	                            this.renderBrokenItemStack(itemstack);
	                            this.setCurrentItemOrArmor(4, (ItemStack)null);
	                        }
	                    }

	                    flag = false;
	                }

	                if (flag)
	                {
	                    this.setFire(8);
	                }
	            }
	        }

	        super.onLivingUpdate();
	    }

	    /**
	     * Called when the entity is attacked.
	     */
	    public boolean attackEntityFrom(DamageSource par1DamageSource, float par2)
	    {
	        if (!super.attackEntityFrom(par1DamageSource, par2))
	        {
	            return false;
	        }
	        else
	        {
	            EntityLivingBase entitylivingbase = this.getAttackTarget();

	            if (entitylivingbase == null && this.getEntityToAttack() instanceof EntityLivingBase)
	            {
	                entitylivingbase = (EntityLivingBase)this.getEntityToAttack();
	            }

	            if (entitylivingbase == null && par1DamageSource.getEntity() instanceof EntityLivingBase)
	            {
	                entitylivingbase = (EntityLivingBase)par1DamageSource.getEntity();
	            }


	            int i = MathHelper.floor_double(this.posX);
	            int j = MathHelper.floor_double(this.posY);
	            int k = MathHelper.floor_double(this.posZ);

	            SummonAidEvent summonAid = ForgeEventFactory.fireZombieSummonAid(this, worldObj, i, j, k, entitylivingbase, this.getEntityAttribute(field_110186_bp).getAttributeValue());
	            
	            if (summonAid.getResult() == Result.DENY)
	            {
	                return true;
	            }
	            else if (summonAid.getResult() == Result.ALLOW || entitylivingbase != null && this.worldObj.difficultySetting == EnumDifficulty.HARD && (double)this.rand.nextFloat() < this.getEntityAttribute(field_110186_bp).getAttributeValue())
	            {
	                EntityZombie entityzombie;
	                if (summonAid.customSummonedAid != null && summonAid.getResult() == Result.ALLOW)
	                {
	                    entityzombie = summonAid.customSummonedAid;
	                }
	                else
	                {
	                    entityzombie = new EntityZombie(this.worldObj);
	                }

	                for (int l = 0; l < 50; ++l)
	                {
	                    int i1 = i + MathHelper.getRandomIntegerInRange(this.rand, 7, 40) * MathHelper.getRandomIntegerInRange(this.rand, -1, 1);
	                    int j1 = j + MathHelper.getRandomIntegerInRange(this.rand, 7, 40) * MathHelper.getRandomIntegerInRange(this.rand, -1, 1);
	                    int k1 = k + MathHelper.getRandomIntegerInRange(this.rand, 7, 40) * MathHelper.getRandomIntegerInRange(this.rand, -1, 1);

	                    if (World.doesBlockHaveSolidTopSurface(this.worldObj, i1, j1 - 1, k1) && this.worldObj.getBlockLightValue(i1, j1, k1) < 10)
	                    {
	                        entityzombie.setPosition((double)i1, (double)j1, (double)k1);

	                        if (this.worldObj.checkNoEntityCollision(entityzombie.boundingBox) && this.worldObj.getCollidingBoundingBoxes(entityzombie, entityzombie.boundingBox).isEmpty() && !this.worldObj.isAnyLiquid(entityzombie.boundingBox))
	                        {
	                            this.worldObj.spawnEntityInWorld(entityzombie);
	                            if (entitylivingbase != null) entityzombie.setAttackTarget(entitylivingbase);
	                            entityzombie.onSpawnWithEgg((IEntityLivingData)null);
	                            this.getEntityAttribute(field_110186_bp).applyModifier(new AttributeModifier("Zombie reinforcement caller charge", -0.05000000074505806D, 0));
	                            entityzombie.getEntityAttribute(field_110186_bp).applyModifier(new AttributeModifier("Zombie reinforcement callee charge", -0.05000000074505806D, 0));
	                            break;
	                        }
	                    }
	                }
	            }

	            return true;
	        }
	    }

	    /**
	     * Called to update the entity's position/logic.
	     */
	    public void onUpdate()
	    {
	        if (!this.worldObj.isRemote && this.isConverting())
	        {
	            int i = this.getConversionTimeBoost();
	            this.conversionTime -= i;

	            if (this.conversionTime <= 0)
	            {
	                this.convertToVillager();
	            }
	        }

	        super.onUpdate();
	    }

	    public boolean attackEntityAsMob(Entity par1Entity)
	    {
	        boolean flag = super.attackEntityAsMob(par1Entity);

	        if (flag)
	        {
	            int i = this.worldObj.difficultySetting.getDifficultyId();

	            if (this.getHeldItem() == null && this.isBurning() && this.rand.nextFloat() < (float)i * 0.3F)
	            {
	                par1Entity.setFire(2 * i);
	            }
	        }

	        return flag;
	    }

	    /**
	     * Returns the sound this mob makes while it's alive.
	     */
	    protected String getLivingSound()
	    {
	        return "mob.zombie.say";
	    }

	    /**
	     * Returns the sound this mob makes when it is hurt.
	     */
	    protected String getHurtSound()
	    {
	        return "mob.zombie.hurt";
	    }

	    /**
	     * Returns the sound this mob makes on death.
	     */
	    protected String getDeathSound()
	    {
	        return "mob.zombie.death";
	    }

	    protected void func_145780_a(int p_145780_1_, int p_145780_2_, int p_145780_3_, Block p_145780_4_)
	    {
	        this.playSound("mob.zombie.step", 0.15F, 1.0F);
	    }

	    protected Item getDropItem()
	    {
	        return Items.rotten_flesh;
	    }

	    /**
	     * Get this Entity's EnumCreatureAttribute
	     */
	    public EnumCreatureAttribute getCreatureAttribute()
	    {
	        return EnumCreatureAttribute.UNDEAD;
	    }

	    protected void dropRareDrop(int par1)
	    {
	        switch (this.rand.nextInt(3))
	        {
	            case 0:
	                this.dropItem(Items.iron_ingot, 1);
	                break;
	            case 1:
	                this.dropItem(Items.carrot, 1);
	                break;
	            case 2:
	                this.dropItem(Items.potato, 1);
	        }
	    }

	    /**
	     * Makes entity wear random armor based on difficulty
	     */
	    protected void addRandomArmor()
	    {
	        super.addRandomArmor();

	        if (this.rand.nextFloat() < (this.worldObj.difficultySetting == EnumDifficulty.HARD ? 0.05F : 0.01F))
	        {
	            int i = this.rand.nextInt(3);

	            if (i == 0)
	            {
	                this.setCurrentItemOrArmor(0, new ItemStack(Items.iron_sword));
	            }
	            else
	            {
	                this.setCurrentItemOrArmor(0, new ItemStack(Items.iron_shovel));
	            }
	        }
	    }

	    /**
	     * (abstract) Protected helper method to write subclass entity data to NBT.
	     */
	    public void writeEntityToNBT(NBTTagCompound par1NBTTagCompound)
	    {
	        super.writeEntityToNBT(par1NBTTagCompound);

	        if (this.isChild())
	        {
	            par1NBTTagCompound.setBoolean("IsBaby", true);
	        }

	        if (this.isVillager())
	        {
	            par1NBTTagCompound.setBoolean("IsVillager", true);
	        }

	        par1NBTTagCompound.setInteger("ConversionTime", this.isConverting() ? this.conversionTime : -1);
	        par1NBTTagCompound.setBoolean("CanBreakDoors", this.func_146072_bX());
	    }

	    /**
	     * (abstract) Protected helper method to read subclass entity data from NBT.
	     */
	    public void readEntityFromNBT(NBTTagCompound par1NBTTagCompound)
	    {
	        super.readEntityFromNBT(par1NBTTagCompound);

	        if (par1NBTTagCompound.getBoolean("IsBaby"))
	        {
	            this.setChild(true);
	        }

	        if (par1NBTTagCompound.getBoolean("IsVillager"))
	        {
	            this.setVillager(true);
	        }

	        if (par1NBTTagCompound.hasKey("ConversionTime", 99) && par1NBTTagCompound.getInteger("ConversionTime") > -1)
	        {
	            this.startConversion(par1NBTTagCompound.getInteger("ConversionTime"));
	        }

	        this.func_146070_a(par1NBTTagCompound.getBoolean("CanBreakDoors"));
	    }

	    /**
	     * This method gets called when the entity kills another one.
	     */
	    public void onKillEntity(EntityLivingBase par1EntityLivingBase)
	    {
	        super.onKillEntity(par1EntityLivingBase);

	        if ((this.worldObj.difficultySetting == EnumDifficulty.NORMAL || this.worldObj.difficultySetting == EnumDifficulty.HARD) && par1EntityLivingBase instanceof EntityVillager)
	        {
	            if (this.rand.nextBoolean())
	            {
	                return;
	            }

	            EntityZombie entityzombie = new EntityZombie(this.worldObj);
	            entityzombie.copyLocationAndAnglesFrom(par1EntityLivingBase);
	            this.worldObj.removeEntity(par1EntityLivingBase);
	            entityzombie.onSpawnWithEgg((IEntityLivingData)null);
	            entityzombie.setVillager(true);

	            if (par1EntityLivingBase.isChild())
	            {
	                entityzombie.setChild(true);
	            }

	            this.worldObj.spawnEntityInWorld(entityzombie);
	            this.worldObj.playAuxSFXAtEntity((EntityPlayer)null, 1016, (int)this.posX, (int)this.posY, (int)this.posZ, 0);
	        }
	    }

	    public IEntityLivingData onSpawnWithEgg(IEntityLivingData par1EntityLivingData)
	    {
	        Object par1EntityLivingData1 = super.onSpawnWithEgg(par1EntityLivingData);
	        float f = this.worldObj.func_147462_b(this.posX, this.posY, this.posZ);
	        this.setCanPickUpLoot(this.rand.nextFloat() < 0.55F * f);

	        if (par1EntityLivingData1 == null)
	        {
	            par1EntityLivingData1 = new EntityZombie.GroupData(this.worldObj.rand.nextFloat() < ForgeModContainer.zombieBabyChance, this.worldObj.rand.nextFloat() < 0.05F, null);
	        }

	        if (par1EntityLivingData1 instanceof EntityZombie.GroupData)
	        {
	            EntityZombie.GroupData groupdata = (EntityZombie.GroupData)par1EntityLivingData1;

	            if (groupdata.field_142046_b)
	            {
	                this.setVillager(true);
	            }

	            if (groupdata.field_142048_a)
	            {
	                this.setChild(true);
	            }
	        }

	        this.func_146070_a(this.rand.nextFloat() < f * 0.1F);
	        this.addRandomArmor();
	        this.enchantEquipment();

	        if (this.getEquipmentInSlot(4) == null)
	        {
	            Calendar calendar = this.worldObj.getCurrentDate();

	            if (calendar.get(2) + 1 == 10 && calendar.get(5) == 31 && this.rand.nextFloat() < 0.25F)
	            {
	                this.setCurrentItemOrArmor(4, new ItemStack(this.rand.nextFloat() < 0.1F ? Blocks.lit_pumpkin : Blocks.pumpkin));
	                this.equipmentDropChances[4] = 0.0F;
	            }
	        }

	        this.getEntityAttribute(SharedMonsterAttributes.knockbackResistance).applyModifier(new AttributeModifier("Random spawn bonus", this.rand.nextDouble() * 0.05000000074505806D, 0));
	        double d0 = this.rand.nextDouble() * 1.5D * (double)this.worldObj.func_147462_b(this.posX, this.posY, this.posZ);

	        if (d0 > 1.0D)
	        {
	            this.getEntityAttribute(SharedMonsterAttributes.followRange).applyModifier(new AttributeModifier("Random zombie-spawn bonus", d0, 2));
	        }

	        if (this.rand.nextFloat() < f * 0.05F)
	        {
	            this.getEntityAttribute(field_110186_bp).applyModifier(new AttributeModifier("Leader zombie bonus", this.rand.nextDouble() * 0.25D + 0.5D, 0));
	            this.getEntityAttribute(SharedMonsterAttributes.maxHealth).applyModifier(new AttributeModifier("Leader zombie bonus", this.rand.nextDouble() * 3.0D + 1.0D, 2));
	            this.func_146070_a(true);
	        }

	        return (IEntityLivingData)par1EntityLivingData1;
	    }

	    /**
	     * Called when a player interacts with a mob. e.g. gets milk from a cow, gets into the saddle on a pig.
	     */
	    public boolean interact(EntityPlayer par1EntityPlayer)
	    {
	        ItemStack itemstack = par1EntityPlayer.getCurrentEquippedItem();

	        if (itemstack != null && itemstack.getItem() == Items.golden_apple && itemstack.getItemDamage() == 0 && this.isVillager() && this.isPotionActive(Potion.weakness))
	        {
	            if (!par1EntityPlayer.capabilities.isCreativeMode)
	            {
	                --itemstack.stackSize;
	            }

	            if (itemstack.stackSize <= 0)
	            {
	                par1EntityPlayer.inventory.setInventorySlotContents(par1EntityPlayer.inventory.currentItem, (ItemStack)null);
	            }

	            if (!this.worldObj.isRemote)
	            {
	                this.startConversion(this.rand.nextInt(2401) + 3600);
	            }

	            return true;
	        }
	        else
	        {
	            return false;
	        }
	    }

	    /**
	     * Starts converting this zombie into a villager. The zombie converts into a villager after the specified time in
	     * ticks.
	     */
	    protected void startConversion(int par1)
	    {
	        this.conversionTime = par1;
	        this.getDataWatcher().updateObject(14, Byte.valueOf((byte)1));
	        this.removePotionEffect(Potion.weakness.id);
	        this.addPotionEffect(new PotionEffect(Potion.damageBoost.id, par1, Math.min(this.worldObj.difficultySetting.getDifficultyId() - 1, 0)));
	        this.worldObj.setEntityState(this, (byte)16);
	    }

	    @SideOnly(Side.CLIENT)
	    public void handleHealthUpdate(byte par1)
	    {
	        if (par1 == 16)
	        {
	            this.worldObj.playSound(this.posX + 0.5D, this.posY + 0.5D, this.posZ + 0.5D, "mob.zombie.remedy", 1.0F + this.rand.nextFloat(), this.rand.nextFloat() * 0.7F + 0.3F, false);
	        }
	        else
	        {
	            super.handleHealthUpdate(par1);
	        }
	    }

	    /**
	     * Determines if an entity can be despawned, used on idle far away entities
	     */
	    protected boolean canDespawn()
	    {
	        return !this.isConverting();
	    }

	    /**
	     * Returns whether this zombie is in the process of converting to a villager
	     */
	    public boolean isConverting()
	    {
	        return this.getDataWatcher().getWatchableObjectByte(14) == 1;
	    }

	    /**
	     * Convert this zombie into a villager.
	     */
	    protected void convertToVillager()
	    {
	        EntityVillager entityvillager = new EntityVillager(this.worldObj);
	        entityvillager.copyLocationAndAnglesFrom(this);
	        entityvillager.onSpawnWithEgg((IEntityLivingData)null);
	        entityvillager.setLookingForHome();

	        if (this.isChild())
	        {
	            entityvillager.setGrowingAge(-24000);
	        }

	        this.worldObj.removeEntity(this);
	        this.worldObj.spawnEntityInWorld(entityvillager);
	        entityvillager.addPotionEffect(new PotionEffect(Potion.confusion.id, 200, 0));
	        this.worldObj.playAuxSFXAtEntity((EntityPlayer)null, 1017, (int)this.posX, (int)this.posY, (int)this.posZ, 0);
	    }

	    /**
	     * Return the amount of time decremented from conversionTime every tick.
	     */
	    protected int getConversionTimeBoost()
	    {
	        int i = 1;

	        if (this.rand.nextFloat() < 0.01F)
	        {
	            int j = 0;

	            for (int k = (int)this.posX - 4; k < (int)this.posX + 4 && j < 14; ++k)
	            {
	                for (int l = (int)this.posY - 4; l < (int)this.posY + 4 && j < 14; ++l)
	                {
	                    for (int i1 = (int)this.posZ - 4; i1 < (int)this.posZ + 4 && j < 14; ++i1)
	                    {
	                        Block block = this.worldObj.getBlock(k, l, i1);

	                        if (block == Blocks.iron_bars || block == Blocks.bed)
	                        {
	                            if (this.rand.nextFloat() < 0.3F)
	                            {
	                                ++i;
	                            }

	                            ++j;
	                        }
	                    }
	                }
	            }
	        }

	        return i;
	    }

	    public void func_146071_k(boolean p_146071_1_)
	    {
	        this.func_146069_a(p_146071_1_ ? 0.5F : 1.0F);
	    }

	    /**
	     * Sets the width and height of the entity. Args: width, height
	     */
	    protected final void setSize(float par1, float par2)
	    {
	        boolean flag = this.field_146074_bv > 0.0F && this.field_146073_bw > 0.0F;
	        this.field_146074_bv = par1;
	        this.field_146073_bw = par2;

	        if (!flag)
	        {
	            this.func_146069_a(1.0F);
	        }
	    }

	    protected final void func_146069_a(float p_146069_1_)
	    {
	        super.setSize(this.field_146074_bv * p_146069_1_, this.field_146073_bw * p_146069_1_);
	    }

	    class GroupData implements IEntityLivingData
	    {
	        public boolean field_142048_a;
	        public boolean field_142046_b;
	        private static final String __OBFID = "CL_00001704";

	        private GroupData(boolean par2, boolean par3)
	        {
	            this.field_142048_a = false;
	            this.field_142046_b = false;
	            this.field_142048_a = par2;
	            this.field_142046_b = par3;
	        }

	        GroupData(boolean par2, boolean par3, Object par4EntityZombieINNER1)
	        {
	            this(par2, par3);
	        }
	    }

}
