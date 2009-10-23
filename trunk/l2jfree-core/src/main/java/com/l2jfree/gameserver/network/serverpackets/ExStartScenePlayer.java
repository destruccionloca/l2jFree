package com.l2jfree.gameserver.network.serverpackets;

/**
 * Shows a movie to the player. After it is shown, client
 * sends us EndScenePlayer with the specified scene ID.<BR>
 * The client MUST be in the correct position, because the
 * camera's position depend's on current character's position.
 * @author savormix
 */
public final class ExStartScenePlayer extends L2GameServerPacket
{
	private static final String _S__FE_99_EXSTARTSCENEPLAYER = "[S] FE:99 ExStartScenePlayer";

	private final int _scene;

	public ExStartScenePlayer(int sceneId)
	{
		_scene = sceneId;
	}

	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x99);

		writeD(_scene);
	}

	@Override
	public String getType()
	{
		return _S__FE_99_EXSTARTSCENEPLAYER;
	}
}
