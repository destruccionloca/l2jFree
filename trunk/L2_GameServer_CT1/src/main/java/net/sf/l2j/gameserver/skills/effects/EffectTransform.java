package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.datatables.TransformationsTable;
import net.sf.l2j.gameserver.datatables.TransformationsTable.L2Transformation;

final class EffectTransform extends L2Effect
{

    public EffectTransform(Env env, EffectTemplate template)
    {
        super(env, template);
    }

    public net.sf.l2j.gameserver.model.L2Effect.EffectType getEffectType()
    {
        return net.sf.l2j.gameserver.model.L2Effect.EffectType.TRANSFORM;
    }

    public void onStart()
    {
        if(getEffected() instanceof L2PcInstance)
        {
            L2PcInstance client = (L2PcInstance)getEffected();
            L2Transformation l2tr = TransformationsTable.getInstance().getTransform(getTransformationId());
            if(l2tr != null)
                client.setTransform(l2tr);
        }
    }

    public void onExit()
    {
        if(getEffected() instanceof L2PcInstance)
            ((L2PcInstance)getEffected()).untransform();
    }

    public boolean onActionTime()
    {
        if(getEffected() instanceof L2PcInstance)
            ((L2PcInstance)getEffected()).untransform();
        return false;
    }
}
