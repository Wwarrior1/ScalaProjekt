package config

object Routes {
  object Todos {
    def all = "/all"
    def create = "/create"
    def update(id: Long) = s"/update/$id"
    def delete(id: Long) = s"/delete/$id"
    def clear = "/clear"
  }
}
