package codechicken.multipart.scalatraits

import java.util.Random

import codechicken.multipart.{IRandomDisplayTickPart, TileMultipartClient}

/**
 * Saves processor time looping on tiles that don't need it
 */
trait TRandomDisplayTickTile extends TileMultipartClient
{
    override def randomDisplayTick(random:Random)
    {
        for(p@(_p: IRandomDisplayTickPart) <- partList.iterator)
            p.randomDisplayTick(random)
    }
}