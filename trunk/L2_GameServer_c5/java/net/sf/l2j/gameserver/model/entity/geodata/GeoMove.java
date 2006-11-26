package net.sf.l2j.gameserver.model.entity.geodata;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2CharPosition;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.StopMove;
import javolution.util.FastMap;


public class GeoMove
{
    public FastMap<Integer,TargetCoord> targetRecorder;
    public int currentTargetId;
    public int currentMoveCounter;
    private L2Character _actor;
    private int _queueSize;
    private static int GEODATAMODE = 1;
    private static int PATHNODEMODE = 2;
    
    public class TargetCoord
    {
        public long targetId;
        public int x;
        public int y;
        public int z;
    }
    
    public GeoMove(L2Character actor)
    {
        targetRecorder = new FastMap<Integer,TargetCoord>();
        _actor = actor;
    }
    public int getQueueSize()
    {
      return _queueSize;  
    }
    
    public synchronized TargetCoord checkMovement(L2Character target)
    {
        TargetCoord  tCoord = null;
        int mode = 2;
        PathFindNode n = null;
        try 
        {
            // Now check Line of sight
            //_log.warning(" first LOS check");
            if ( GeoDataRequester.getInstance().hasMovementLoS(_actor,target).LoS == false )
            {
                //_log.warning(" first no LOS");
                if ( currentTargetId != target.getObjectId())
                {
                    targetRecorder = null;
                    currentTargetId = target.getObjectId();
                }
        
                if (targetRecorder == null)
                {
                    targetRecorder = new FastMap<Integer,TargetCoord>();
                }
                
                if (targetRecorder.size() > 1)
                {
                    // try to get best movement path
                    tCoord = targetRecorder.tail().getPrevious().getValue();
                    if ( Math.abs((tCoord.x - target.getX()) + (tCoord.y - target.getY())) < 1800 )
                    {
                        mode = GEODATAMODE;
                    }
                    else
                    {
                        mode = PATHNODEMODE;
                    }
                      n = new PathFindNode(tCoord.x, tCoord.y, (short)tCoord.z, target.getX(), target.getY(), (short)target.getZ(),mode);
                    
                    if (mode == 1 && n.path == null)
                    {
                        n = new PathFindNode(tCoord.x, tCoord.y, (short)tCoord.z, target.getX(), target.getY(), (short)target.getZ(),2);
                    }
                    if (n.path == null)
                    {
                        FarPoint fp = GeoDataRequester.getInstance().hasMovementLoS(_actor, target.getX(),target.getY(),(short)target.getZ());
                        tCoord.x = fp.x;
                        tCoord.y = fp.y;
                        tCoord.z = fp.z;
                        return tCoord; 
                    }    
                    if ( ! n.path.getNodes().isEmpty())
                    {
                        _queueSize = n.path.nodes.size();
                        
                        for (Node p : n.path.getNodes())
                        {
                                // target moved, record
                                currentMoveCounter ++;
                                tCoord = new TargetCoord();
                                tCoord.targetId  = target.getObjectId();
                                tCoord.x = p.location.getX();
                                tCoord.y = p.location.getY();
                                tCoord.z = p.location.getZ();
                                targetRecorder.put(currentMoveCounter,tCoord);
                                //_log.warning("Recording id : " + tCoord.targetId + " X:"  + tCoord.x + " Y:" + tCoord.y + " Z:" +tCoord.z) ;
                        }
                    }
                } 
                else 
                {
                    currentMoveCounter= 0;
                    if ( Math.abs((_actor.getX() - target.getX()) + (_actor.getY() - target.getY())) < 1800 )
                    {
                        mode = GEODATAMODE;
                    }
                    else
                    {
                        mode = PATHNODEMODE;
                    }
                    n = new PathFindNode(_actor.getX(), _actor.getY(), (short)_actor.getZ(), target.getX(), target.getY(), (short)target.getZ(),mode);
                    
                    if (mode == GEODATAMODE && n.path == null)
                    {
                        n = new PathFindNode(_actor.getX(), _actor.getY(), (short)_actor.getZ(), target.getX(), target.getY(), (short)target.getZ(),PATHNODEMODE);
                    }
                    if (n.path == null)
                    {
                        FarPoint fp = GeoDataRequester.getInstance().hasMovementLoS(_actor, target.getX(),target.getY(),(short)target.getZ());
                        tCoord.x = fp.x;
                        tCoord.y = fp.y;
                        tCoord.z = fp.z;
                        return tCoord; 
                    }    
                    if ( ! n.path.getNodes().isEmpty())
                    {
                        _queueSize = n.path.nodes.size();
                        for (Node p : n.path.getNodes())
                        {
                                // target moved, record
                                currentMoveCounter ++;
                                tCoord = new TargetCoord();
                                tCoord.targetId  = target.getObjectId();
                                tCoord.x = p.location.getX();
                                tCoord.y = p.location.getY();
                                tCoord.z = p.location.getZ();
                                targetRecorder.put(currentMoveCounter,tCoord);
                                //_log.warning("Recording id : " + tCoord.targetId + " X:"  + tCoord.x + " Y:" + tCoord.y + " Z:" +tCoord.z) ;
                         }
                     }    
                }
                return checkmap();
            }
            _queueSize = 0;
            return null;
        }
        catch( Exception e)
        {
            return null; 
        }
    }
    public synchronized TargetCoord checkMovement(L2CharPosition target)
    {
        TargetCoord  tCoord = null;
        int mode = PATHNODEMODE;
        PathFindNode n = null;
        long ti = System.currentTimeMillis();
        
        FarPoint fp = new FarPoint();
        
        // Now check Line of sight
        try
        {
            if (_actor instanceof L2PcInstance)
            {
                if (((L2PcInstance)_actor).getAccessLevel() >= 100 && Config.ALLOW_GEODATA_DEBUG)
                {
                    _actor.sendMessage("First GeoMove x:" + target.x + " y:" + target.y + " z:" + target.z);
                }
            }
            
            fp =  GeoDataRequester.getInstance().hasMovementLoS(_actor,target.x,target.y,target.z);
            
            if (fp.LoS == false )
            {
                if (_actor instanceof L2PcInstance)
                {
                    if (((L2PcInstance)_actor).getAccessLevel() >= 100 && Config.ALLOW_GEODATA_DEBUG)
                    {
                        _actor.sendMessage("No LoS on move fp =x:" + fp.x + "  y:" + fp.y + " z:" + fp.z );
                    }
                }
                //_log.warning(" first no LOS");
                if ( currentTargetId != target.getObjectId())
                {
                    targetRecorder = null;
                    currentTargetId = target.getObjectId();
                }
                        
                if (targetRecorder == null)
                {
                    targetRecorder = new FastMap<Integer,TargetCoord>();
                    if (_actor instanceof L2PcInstance)
                    {
                        if (((L2PcInstance)_actor).getAccessLevel() >= 100 && Config.ALLOW_GEODATA_DEBUG)
                        {
                            _actor.sendMessage(" New target Recorder");
                        }
                    }
                }

                if (targetRecorder.size() < 1)
                {
                    currentMoveCounter= 0;
                    if ( Math.abs((_actor.getX() - target.x) + (_actor.getY() - target.y)) < 1800 )
                    {
                        mode = GEODATAMODE;
                    }
                    else
                    {
                        mode = PATHNODEMODE;
                    }
                    n = new PathFindNode(_actor.getX(), _actor.getY(), (short)_actor.getZ(), target.x, target.y, (short)target.z,mode);
                    
                    //if (mode == GEODATAMODE && n.path == null)
                    //{
                    //   n = new PathFindNode(_actor.getX(), _actor.getY(), (short)_actor.getZ(), target.x, target.y, (short)target.z,PATHNODEMODE);
                    //}

                    if ( n.path == null )
                    {
                        tCoord = new TargetCoord();
                        //fp =  GeoDataRequester.getInstance().hasMovementLoS(_actor,target.x,target.y,target.z);
                        tCoord.x = fp.x;
                        tCoord.y = fp.y;
                        tCoord.z = fp.z;
                        _queueSize = 1;
                        
                        if (_actor instanceof L2PcInstance)
                        {
                            if (((L2PcInstance)_actor).getAccessLevel() >= 100 && Config.ALLOW_GEODATA_DEBUG)
                            {
                                _actor.sendMessage(" path==null; FP= x:" + fp.x + "  y:" + fp.y + " z:" + fp.z);
                                _actor.sendMessage("Time fo find null = "+ (System.currentTimeMillis() - ti)+ "ms");
                            }
                        }
                        // not on the same level path .. return FP
                        return tCoord;
                    }
                    if ( ! n.path.getNodes().isEmpty())
                    {
                        _queueSize = n.path.getNodes().size();
                        
                        for (Node p : n.path.getNodes())
                        {
                                // target moved, record
                                currentMoveCounter ++;
                                tCoord = new TargetCoord();
                                tCoord.targetId  = target.getObjectId();
                                tCoord.x = p.location.getX();
                                tCoord.y = p.location.getY();
                                tCoord.z = p.location.getZ();
                                if (_actor instanceof L2PcInstance)
                                {
                                    if (((L2PcInstance)_actor).getAccessLevel() >= 100 && Config.ALLOW_GEODATA_DEBUG)
                                    {
                                        _actor.sendMessage("path = x:" + tCoord.x + " y:" + tCoord.y + " z:" + tCoord.z);
                                    }
                                }
                                targetRecorder.put(currentMoveCounter,tCoord);
                                //_log.warning("Recording id : " + tCoord.targetId + " X:"  + tCoord.x + " Y:" + tCoord.y + " Z:" +tCoord.z) ;
                        }
                        if (_actor instanceof L2PcInstance)
                        {
                            if (((L2PcInstance)_actor).getAccessLevel() >= 100 && Config.ALLOW_GEODATA_DEBUG)
                            {
                                _actor.sendMessage("Time to find path:" + (System.currentTimeMillis() - ti) + "ms  Total Nodes:" + n.path.getNodes().size());
                            }
                        }
                     }    
                }
                return checkmap();
            }
            _queueSize = 0;
            return null;
        }
        catch (Exception e)
        {
            if (_actor instanceof L2PcInstance)
            {
                if (((L2PcInstance)_actor).getAccessLevel() >= 100 && Config.ALLOW_GEODATA_DEBUG)
                {
                    _actor.sendMessage("CheckMovement excep: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            return null;
        }
    }

    private synchronized TargetCoord checkmap()
    {

        TargetCoord  tCoord = null;
        TargetCoord  prevtCoord = null;
        //int failSafeCounter = 0;
        try 
        {
            if (targetRecorder == null)
            {
                _queueSize = 0;
                return null;
            }
            
            int mapSize = targetRecorder.size();
            
            if (mapSize == 0 )
            {
                _queueSize = 0;
                return null;
            }
            
            int startMapPos = targetRecorder.head().getNext().getKey();
            int lastMapPos = targetRecorder.tail().getPrevious().getKey();
            int mapPos = startMapPos;

            prevtCoord = targetRecorder.get(mapPos);
            tCoord = prevtCoord;
    
            while (mapPos < lastMapPos &&
                      GeoDataRequester.getInstance().hasMovementLoS
                      ( _actor, tCoord.x, tCoord.y, tCoord.z).LoS == true)
            {
               prevtCoord = tCoord;
               mapPos++;
               tCoord = targetRecorder.get(mapPos);    
            }
            for (int i = startMapPos ; i < mapPos -1 ; i++)
            {
                targetRecorder.remove(i);
            }
            _queueSize = targetRecorder.size();
            
            if (_actor instanceof L2PcInstance)
            {
                if (((L2PcInstance)_actor).getAccessLevel() >= 100 && Config.ALLOW_GEODATA_DEBUG)
                {
                    _actor.sendMessage("moving to x:" + prevtCoord.x + " y:" + prevtCoord.y + " z:" + prevtCoord.z + " Qsize:" + _queueSize ); 
                }
            }
            
            return prevtCoord;
        }
        catch (Exception e)
        {
            if (_actor instanceof L2PcInstance)
            {
                _queueSize = 0;
                if (((L2PcInstance)_actor).getAccessLevel() >= 100 && Config.ALLOW_GEODATA_DEBUG)
                {
                    _actor.sendMessage("CheckMap excep : " + e.getMessage());
                }
            }
            return null;
        }
    }
}
