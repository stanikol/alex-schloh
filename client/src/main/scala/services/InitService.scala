package services

import models.RequestMessage
import utils.{Logging, NetworkMessage}

object InitService {
  def init(sendMessage: (RequestMessage) => Unit, connect: () => Unit) {
    Logging.installErrorHandler()
    NetworkMessage.register(sendMessage)

    Logging.debug("AlexShop has started.")
    connect()
  }
}
